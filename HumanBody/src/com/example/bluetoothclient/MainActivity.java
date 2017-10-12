package com.example.bluetoothclient;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Set;
import java.util.StringTokenizer;

import android.app.Activity;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;

public class MainActivity extends Activity implements OnClickListener, OnItemClickListener , OnTouchListener{

	String temp = "";
	ArrayList<String> ipAddress = new ArrayList<String>();
	Button  buttonBrowse, buttonPaste;
	ProgressBar progress;
	Dialog dialogBrowse;
	public static final int SERVER_PORT = 9999;
	public static final String STATUS_MESSAGE = "message";
	public static final String STATUS_PICTURE = "picture";
	public static final int PACKET_SIZE = 50000;
	static String appName = "BluetoothClient";
	String debug = "";
	EditText messageEdit;
	EditText editTextPaste;
	Handler handlerUpdateTextMessage;
	StringBuilder messages = new StringBuilder("");
	ImageView imageView;
	
	String imagePath = "";
	boolean isPicture = false;
	
	Dialog dialogProcessing;
	Handler handlerShowDialog;
	Handler handlerDismissDialog;
	Handler handlerSetImage;
	
	
	String recievedImagePath = "";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		
//		try{
//			ipAddress.add("192.168.1.3");
//		}catch(Exception ex){ex.printStackTrace();}
		messageEdit = (EditText)findViewById(R.id.editTextMessage);
		progress = (ProgressBar)findViewById(R.id.progressBar1);
		buttonBrowse = (Button)findViewById(R.id.buttonBrowse);
		dialogBrowse = new ListDialog(MainActivity.this, this);
		imageView = (ImageView)findViewById(R.id.imageView1);
		dialogProcessing = new Dialog(MainActivity.this);
		dialogProcessing.setContentView(R.layout.dialog_processing);
		buttonPaste = (Button)findViewById(R.id.buttonPaste);
		buttonPaste.setOnClickListener(this);
		
		buttonBrowse.setOnClickListener(this);
		messageEdit.setOnTouchListener(this);
		
		handlerUpdateTextMessage = new Handler(){
			@Override
			public void handleMessage(Message msg){
				//editTextPaste.setText(messages.toString());
				buttonPaste.setText(messages.toString());
				buttonPaste.setBackgroundResource(0);
			}
		};
		
		handlerShowDialog = new Handler(){
			@Override
			public void handleMessage(Message msg){
				dialogProcessing.show();
			}
		};
		handlerDismissDialog = new Handler(){
			@Override
			public void handleMessage(Message msg){
				dialogProcessing.dismiss();
			}
		};
		handlerSetImage = new Handler(){
			@Override
			public void handleMessage(Message msg){
				setImage(recievedImagePath);
			}
		};
		try{
			handlerShowDialog.sendEmptyMessage(0);
			addingIPS();
		}catch(Exception ex){ex.printStackTrace();}
		handlerDismissDialog.sendEmptyMessage(0);
		// starting server on bluetooth
		new AcceptThread().start();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	@Override
	public void onClick(View v) {
		if(v == buttonBrowse){
			dialogBrowse.show();
		}
		else if(v == buttonPaste){
			try{
				//String s = messageEdit.getText().toString();
				Log.e("OnClick", "buttonPaste");
				new ConnectDevicesThread().start();
				debug="";	
			}catch(Exception ex){ 
				ex.printStackTrace();
			}
		}
		
	}// end onClick
	
	@Override
	public void onItemClick(AdapterView<?> arg0, View view, int index, long arg3) {
		
	}

	public void reciveMessage(DataInputStream in)throws Exception{
		String s = in.readLine();
		messages = new StringBuilder(s);
		Log.e("MainActivity",messages.toString());
		handlerUpdateTextMessage.sendEmptyMessage(0);
		if(in!=null)in.close();
	}
	
	public void sendMessage(OutputStream out)throws Exception{
		out.write((messageEdit.getText().toString()+"\n").getBytes());
		out.flush();
		Log.e(appName, "Flushed");
		Thread.sleep(500);
		if(out!=null)out.close();	
	}

	public void sendPictureNew(PrintStream out)throws Exception{
		Log.e("sendPictureNew:", "Path:"+imagePath);
		File f = new File(imagePath);
		
		if(!imagePath.trim().equals("")){
//			long size =f.getTotalSpace();
			
			
			DataInputStream input = new DataInputStream(new FileInputStream(f));
			int size = input.available();
			byte[] buffer = new byte[size];
	
			Log.e("packet", "size(int): "+((int)size));
			Log.e("packet", "size: "+size);
			
			out.println(f.getName()+"\r");
			Log.e("f.getName()", "f.getName(): "+f.getName());
			out.println(size+"\r");  // sending size
//			out.flush();
			
			input.readFully(buffer, 0, size);
			out.write(buffer, 0, size);
			out.flush();
		
			Log.e("MainActivity", "Send Picture Successfully..!");
//			Thread.sleep(5000);
			out.close();
			input.close();
		}
	}
	
	public void recievePictureNew(DataInputStream in)throws Exception{
		Log.e("MainActivity", "Recieving Piture");
//		DataInputStream dataInputStream = new DataInputStream(in);

		Log.e("MainActivity", "InputStream created: "+in);
		String fileName = in.readLine();
		Log.e("MainActivity", "File Name:"+fileName);
		int size = Integer.parseInt(in.readLine());
		Log.e("MainActivity", "size:"+size);
		File f = new File(Environment.getExternalStorageDirectory(),fileName);
		//if(f.exists()){f.delete();}
		//f.createNewFile();
		Log.e("recievePiture", "File created:"+f.getAbsolutePath());
		Log.e("recievePiture", "Size:"+size);
		
		FileOutputStream output = new FileOutputStream(f.getAbsolutePath());
		recievedImagePath = f.getAbsolutePath();
		
		byte[] buffer = new byte[size];
		in.readFully(buffer, 0, size);
		output.write(buffer, 0, size);
		//output.flush();
		
		output.close();
		in.close();
		Log.e("packet", "recieved Pictured..!");
		
		handlerSetImage.sendEmptyMessage(0);
	}

	// Server Class for accepting the request and sending data
	private class AcceptThread extends Thread {
	    ServerSocket server;
	 
	    public void run() {
	        Socket socket = null;
	        try{
	        	server = new ServerSocket(SERVER_PORT);
	        // Keep listening until exception occurs or a socket is returned
		        while (true) {
		        	try {
		        		Log.e(appName, "Listening..");
		            	socket = server.accept();
		            
		            	PrintStream out=new PrintStream(socket.getOutputStream());
		            	
		            	if(isPicture){
		            		out.println(STATUS_PICTURE+"\r");
		            		sendPictureNew(out);
		            	}else{
		            		out.println(STATUS_MESSAGE+"\r");
		            		sendMessage(out);
		            	}
		            	//out.close();
	                	socket.close();
	                	Log.e(appName, "Connection Closed");
		        	} catch (IOException e) {
		            	Log.e(appName,">> "+ e.getMessage());
		            	//continue;
		            	//break;
		            }catch (Exception e) {
		            	Log.e(appName,">> "+ e.getMessage());
					}
		            
		        }
	        }catch(Exception ex){ex.printStackTrace();}
	    }
	 
	}
	
	@Override
	public boolean onTouch(View vie, MotionEvent arg1) {
		if(vie == messageEdit){
			Log.e("isPicture"," false");
			isPicture = false;
		}

		return false;
	}
	
	public void setImage(String path){
		File f = new File(path);
		if(f.exists()){
//			imageView.setImageBitmap(BitmapFactory.decodeFile(path));
			
			Bitmap myBitmap =  BitmapFactory.decodeFile(path);
			Drawable myDrawable = new BitmapDrawable(getResources(), myBitmap);
			buttonPaste.setBackgroundDrawable(myDrawable);
			buttonPaste.setText("");
		}else{
			Log.e("MainActivity", "Notify: File doesnt exist");
		}
		
		this.imagePath = path;
		isPicture = true;
	}
	
	
	
	private class ConnectDevicesThread extends Thread{
		
		@Override
		public void run(){
			handlerShowDialog.sendEmptyMessage(0);
    		for(int i=0;i<ipAddress.size();i++){
				try {
		            //BluetoothDevice device = (BluetoothDevice)devices.get(i);
		            
		            // MY_UUID is the app's UUID string, also used by the server code
		            Socket mmSocket = new Socket(ipAddress.get(i), SERVER_PORT);
		            Log.e(appName,"Make Connection..."+mmSocket.getInetAddress().getHostName());

			        DataInputStream in = new DataInputStream(mmSocket.getInputStream());
			        String status = in.readLine();
			        Log.e("status",""+status);
			        if(status.equalsIgnoreCase(STATUS_MESSAGE)){
			        	   	reciveMessage(in);
			        }else{
			        		recievePictureNew(in);
			        }
			        if(in != null)in.close();
			        mmSocket.close();
			        handlerDismissDialog.sendEmptyMessage(0);
				} catch (IOException e) { e.printStackTrace(); handlerDismissDialog.sendEmptyMessage(0);}
				catch(Exception ex){ex.printStackTrace(); handlerDismissDialog.sendEmptyMessage(0);}
			}	
		}
	}// end of method
	
	

	public void addingIPS(){
		try{
			for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
	            NetworkInterface intf = en.nextElement();
	            Log.e(appName, intf.toString());
	            for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
	                InetAddress inetAddress = enumIpAddr.nextElement();
	                String ip =  inetAddress.getHostAddress();
	                StringTokenizer tokens = new StringTokenizer(ip, ".");
	                
	                if(tokens.countTokens() == 4 && ip.length() <= 15 & (!ip.startsWith("127"))){
	                	String subnet = ip.substring(0, ip.lastIndexOf('.'));
			
		                //discovering devices..
		                int timeout=2000;
		                for (int i=2;i<5;i++){
		                	String host=subnet + "." + i;
		                	InetAddress inetAddr = InetAddress.getByName(host);
		                	Log.e(appName, "IP>>"+ip);
		                	if(ip.trim().equals(host))continue;
		                	Log.e(appName,host);
		                	if(InetAddress.getByName(host).isReachable(timeout)){
		                		
		                		Log.e(appName, "HOST>>"+host);
		                		ipAddress.add(host);
		                	}
		                }// end of loop
	                }
	            }// end of inner loop
	            
	        }// end of outer loop
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}
	
}
