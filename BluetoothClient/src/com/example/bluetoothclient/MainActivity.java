package com.example.bluetoothclient;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;

import android.app.Activity;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.BitmapFactory;
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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

public class MainActivity extends Activity implements OnClickListener, OnItemClickListener , OnTouchListener{

	String temp = "";
	Set<BluetoothDevice> pairedDevices;
	
	Button detect , buttonCancel, buttonBrowse, buttonPaste;
	ListView devicesList;
	ProgressBar progress;
	Dialog dialogBrowse;
	ArrayList<BluetoothDevice> devices = new ArrayList<BluetoothDevice>();
	BluetoothAdapter mBluetoothAdapter;
	ArrayAdapter<String> mNewDevicesArrayAdapter;
	UUID SERVER_UUID = UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66");
	//UUID ServerUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
	public static final String STATUS_MESSAGE = "message";
	public static final String STATUS_PICTURE = "picture";
	public static final int PACKET_SIZE = 9216;
	public static final int SLEEP_TIME = 5;
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
		
		detect = (Button)findViewById(R.id.buttonDetect);
		devicesList = (ListView)findViewById(R.id.listViewDevices);
		messageEdit = (EditText)findViewById(R.id.editTextMessage);
		//editTextPaste = (EditText)findViewById(R.id.editTextPaste);
		buttonCancel = (Button)findViewById(R.id.buttonCancelDiscovery);
		progress = (ProgressBar)findViewById(R.id.progressBar1);
		buttonBrowse = (Button)findViewById(R.id.buttonBrowse);
		dialogBrowse = new ListDialog(MainActivity.this, this);
		imageView = (ImageView)findViewById(R.id.imageView1);
		dialogProcessing = new Dialog(MainActivity.this);
		dialogProcessing.setContentView(R.layout.dialog_processing);
		buttonPaste = (Button)findViewById(R.id.buttonPaste);
		buttonPaste.setOnClickListener(this);
		
		detect.setOnClickListener(this);
		buttonCancel.setOnClickListener(this);
		buttonBrowse.setOnClickListener(this);
		devicesList.setOnItemClickListener(this);
		messageEdit.setOnTouchListener(this);
		
		mBluetoothAdapter= BluetoothAdapter.getDefaultAdapter();
		pairedDevices = mBluetoothAdapter.getBondedDevices();
		
		mNewDevicesArrayAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1); 
		devicesList.setAdapter(mNewDevicesArrayAdapter);	
		
		handlerUpdateTextMessage = new Handler(){
			@Override
			public void handleMessage(Message msg){
				//editTextPaste.setText(messages.toString());
				buttonPaste.setText(messages.toString());
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
//		editTextPaste.setOnTouchListener(this);
		
		// starting server on bluetooth
		new AcceptThread(mBluetoothAdapter,SERVER_UUID).start();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	@Override
	public void onClick(View v) {
		if(v == detect){
			progress.setVisibility(View.VISIBLE);
			final BroadcastReceiver mReceiver = new BroadcastReceiver() 
            { 
                public void onReceive(Context context, Intent intent) 
                {
                    String action = intent.getAction(); 
                    // When discovery finds a device 
                    if (BluetoothDevice.ACTION_FOUND.equals(action)) 
                    {
                    	// Get the BluetoothDevice object from the Intent 
                    	BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    	if(!devices.contains(device)){
                    		devices.add(device);
                    		Toast.makeText(getApplicationContext(),"BlueTooth Testing"+device.getName() + "\n"+ device.getAddress() , Toast.LENGTH_SHORT).show(); 
                        	String str = device.getName() + "\n"+ device.getAddress();
                        	mNewDevicesArrayAdapter.add(str);
                    	}
                    	
                    }
                }    
            };

            String aDiscoverable = BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE;
            startActivityForResult(new Intent(aDiscoverable),0xDEADBEEF);
            IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND); 
            registerReceiver(mReceiver, filter); 
            mBluetoothAdapter.startDiscovery();
		}// end if
		
		else if(v == buttonCancel){
			try{
				progress.setVisibility(View.INVISIBLE);
				if(mBluetoothAdapter.isDiscovering())mBluetoothAdapter.cancelDiscovery();
			}catch(Exception ex){ex.printStackTrace();}
			
		}else if(v == buttonBrowse){
			dialogBrowse.show();
		}
		
		if(v == buttonPaste){
			try{
				String s = messageEdit.getText().toString();
				//if(!s.isEmpty())
					//new ConnectThread(devices.get(index)).start();
				new ConnectDevicesThread().start();
				debug="";	
			}catch(Exception ex){ 
				ex.printStackTrace();
			}
		}
		
	}// end onClick
	
	@Override
	public void onItemClick(AdapterView<?> arg0, View view, int index, long arg3) {
		try{
			String s = messageEdit.getText().toString();
			//if(!s.isEmpty())
				//new ConnectThread(devices.get(index)).start();
			new ConnectDevicesThread().start();
			debug="";	
		}catch(Exception ex){ 
			ex.printStackTrace();
		}
	}

private class ConnectThread extends Thread {
	    private final BluetoothSocket mmSocket;
	    private final BluetoothDevice mmDevice;
	 
	    public ConnectThread(BluetoothDevice device) {
	        // Use a temporary object that is later assigned to mmSocket,
	        // because mmSocket is final
	        BluetoothSocket tmp = null;
	        mmDevice = device;
	 
	        // Get a BluetoothSocket to connect with the given BluetoothDevice
	        try {
	            // MY_UUID is the app's UUID string, also used by the server code
	            tmp = device.createRfcommSocketToServiceRecord(SERVER_UUID);
	            Log.e(appName,"Make Connection...");
	            debug+="\nMake Connection...";
	            Toast.makeText(getApplicationContext(), "Make Connection ", Toast.LENGTH_SHORT).show();
	        } catch (IOException e) { e.printStackTrace(); debug+=e.getMessage();  Toast.makeText(getApplicationContext(), "Exception ", Toast.LENGTH_SHORT).show(); }
	        mmSocket = tmp;
	    }
	 
	    public void run() {
	    	handlerShowDialog.sendEmptyMessage(0);
    		
	    	// Cancel discovery because it will slow down the connection
	    	try {
	    		debug += "Canceling Discovering..";
	    		if(mBluetoothAdapter.isDiscovering())mBluetoothAdapter.cancelDiscovery();
	    		Log.e(appName,"Cancel Discovering..");
	    		debug += "Cancel Discovering..";
	        
	            // Connect the device through the socket. This will block
	            // until it succeeds or throws an exception
	            mmSocket.connect();
	            Log.e(appName,"Connected..");
	            debug += "Connected..";
	        } catch (IOException connectException) {
	            // Unable to connect; close the socket and get out
	        	debug+=connectException.getMessage();
	        	connectException.printStackTrace();
	        	try {
	                mmSocket.close();
	                Log.e(appName,"closed");
	        	} catch (IOException closeException) { debug+=closeException.getMessage(); closeException.printStackTrace(); }
	            handlerDismissDialog.sendEmptyMessage(0);
	            Log.e("Mani", "error connecting");
	            //Toast.makeText(getApplicationContext(), "connection error",Toast.LENGTH_SHORT ).show();
	        	return;
	        }//end catch
	 
	        // Do work to manage the connection (in a separate thread)
	        Log.e(appName,"managing..");
	        debug+="managing..";
	        try{ 
	        	String name = mmSocket.getRemoteDevice().getName();
            	Log.e(appName, "Connected With "+mmSocket);
                
            	InputStream in = mmSocket.getInputStream();
            	String status = readLine(in);
            	if(status.equalsIgnoreCase(STATUS_MESSAGE)){
            		reciveMessage(in);
            	}else{
            		recievePicture(in);
            	}
            	
	        }catch(Exception ex){ex.printStackTrace(); debug+=ex.getMessage();}
	        cancel();
	        handlerDismissDialog.sendEmptyMessage(0);
	    }
	 
	   // Will cancel an in-progress connection, and close the socket 
	    public void cancel() {
	        try {
	            if(mmSocket !=null)mmSocket.close();
	            Log.e(appName,"canceled..");
	        } catch (IOException e) {e.printStackTrace(); debug+=e.getMessage(); }
	    }
	}

	public void reciveMessage(InputStream in)throws Exception{
		String s = readLine(in);
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


	public void sendPicture(OutputStream out)throws Exception{
		Log.e("MainActivity", "Sending Piture");
		DataOutputStream output = new DataOutputStream(out);
		File f = new File(imagePath);
		
		DataInputStream input = new DataInputStream(new FileInputStream(f));
		String extention = imagePath.substring(imagePath.lastIndexOf(".")+1 , imagePath.length());
		if(!imagePath.trim().equals("")){
			byte[] buffer = new byte[PACKET_SIZE];
			long size =f.getTotalSpace();
			long totalPackets = size / PACKET_SIZE;
			long remainingPackets = size % PACKET_SIZE;
			
			
			Log.e("packet", "total: "+totalPackets);
			//sending protocol
			output.write((extention+"\n").getBytes());
			output.write((""+size+"\n").getBytes());  // sending size
			output.flush();
			
			for(int i=1;i<=totalPackets;i++){
				input.readFully(buffer, 0, buffer.length);
				output.write(buffer, 0, buffer.length);
				output.flush();
				if(i%100 == 0){
					Log.e("Transfering", "packet: "+i);
				}
			}
			input.readFully(buffer, 0, (int)remainingPackets);
			output.write(buffer, 0, (int)remainingPackets);
			
			input.close();
			out.close();
		}
	}
	
	public void recievePicture(InputStream  input)throws Exception{
		Log.e("MainActivity", "Recieving Piture");
		DataInputStream in = new DataInputStream(input);
		byte[] buffer = new byte[PACKET_SIZE]; 
		//recieving protocol
		String extension = readLine(in);
		
		File f = new File(Environment.getExternalStorageDirectory(),"Touch"+Math.round(Math.random())+"."+extension);
		f.createNewFile();
		Log.e("packet", "file created:"+f.getAbsolutePath());
		DataOutputStream output = new DataOutputStream(new FileOutputStream(f));
		long size = Long.parseLong(readLine(in));
		Log.e("packet", "size"+size);
		long totalPackets = size / PACKET_SIZE;
		long remainingPackets = size % PACKET_SIZE;
		
		Log.e("packet", "total: "+totalPackets);
		
		for(int i=1;i<=totalPackets;i++){
			in.readFully(buffer, 0, buffer.length);
			output.write(buffer, 0, buffer.length);
			output.flush();
			if(i%100 == 0){
				Log.e("Transfering", "packet: "+i);
				Thread.sleep(100);
			}
		}
		in.readFully(buffer, 0, (int)remainingPackets);
		output.write(buffer, 0, (int)remainingPackets);
		
		output.close();
		in.close();
		Log.e("packet", "recieved");
		
		recievedImagePath = f.getAbsolutePath();
		handlerSetImage.sendEmptyMessage(0);
	}

	// Server Class for accepting the request and sending data
	private class AcceptThread extends Thread {
	    BluetoothServerSocket mmServerSocket;
	    BluetoothAdapter mBluetoothAdapter;
	    UUID MY_UUID;
	    public AcceptThread(BluetoothAdapter mBluetoothAdapter, UUID MY_UUID) {
	       this.mBluetoothAdapter = mBluetoothAdapter;
	       this.MY_UUID = MY_UUID;
	    }
	 
	    public void run() {
	        BluetoothSocket socket = null;
	        // Keep listening until exception occurs or a socket is returned
	       while (true) {
	        	try {
	        		// MY_UUID is the app's UUID string, also used by the client code
	        		BluetoothServerSocket tmp = mBluetoothAdapter.listenUsingRfcommWithServiceRecord(appName, MY_UUID);
		            mmServerSocket = tmp;
		            
		            Log.e(appName, "Listening..");
	            	socket = mmServerSocket.accept();
	            
	            	OutputStream out=socket.getOutputStream();
	            	
	            	if(isPicture){
	            		out.write((STATUS_PICTURE+"\n").getBytes());
	            		sendPicture(out);
	            	}else{
	            		out.write((STATUS_MESSAGE+"\n").getBytes());
	            		sendMessage(out);
	            	}
	            	out.close();
                	mmServerSocket.close();
                	Log.e(appName, "Connection Closed");
	        	} catch (IOException e) {
	            	Log.e(appName,">> "+ e.getMessage());
	            	continue;
	            	//break;
	            }catch (Exception e) {
	            	Log.e(appName,">> "+ e.getMessage());
				}
	            
	        }
	    }
	 
	    /** Will cancel the listening socket, and cause the thread to finish */
	    public void cancel() {
	        try {
	            mmServerSocket.close();
	        } catch (IOException e) {e.printStackTrace(); }
	    }
	}
	
	private String readLine(InputStream in)throws Exception{
		
		StringBuffer buffer=new StringBuffer();		
			
		int ch=in.read();
    	while(ch!='\n' && ch!=-1){
    	    buffer.append((char)ch);
    		ch=in.read();
    	}
    	
		return buffer.toString();
	}
	
	@Override
	public boolean onTouch(View vie, MotionEvent arg1) {
		if(vie == messageEdit){
			Log.e("isPicture"," false");
			isPicture = false;
		}
//		if(vie == editTextPaste)new Thread(){
//			public void run(){
//				for(int i=0;i<devices.size();i++){
//					try{
//						BluetoothDevice btDevice = (BluetoothDevice)devices.get(i);
//						BluetoothSocket socket =btDevice.createRfcommSocketToServiceRecord(SERVER_UUID);
//						InputStream in = socket.getInputStream();
//		            	String s = readLine(in);
//		                messages.append(s);
//		                handlerUpdateTextMessage.sendEmptyMessage(0);
//		                socket.close();
//					}catch(Exception ex){
//						Log.e("MainActiity OnTouch", "Can't connect");
//					}
//				}
//			}
//		}.start();
		return false;
	}
	
	public void setImage(String path){
		File f = new File(path);
		if(f.exists())imageView.setImageBitmap(BitmapFactory.decodeFile(path));
		else{
			Log.e("MainActivity", "Notify: File doesnt exist");
		}
		
		this.imagePath = path;
		isPicture = true;
	}
	
	
	
	private class ConnectDevicesThread extends Thread{
		
		@Override
		public void run(){
			Iterator<BluetoothDevice> iterator = pairedDevices.iterator();
			handlerShowDialog.sendEmptyMessage(0);
    		//for(int i=0;i<devices.size();i++){
			while(iterator.hasNext()){
				try {
		            //BluetoothDevice device = (BluetoothDevice)devices.get(i);
		            BluetoothDevice device = iterator.next();
					
		            // MY_UUID is the app's UUID string, also used by the server code
		            BluetoothSocket mmSocket = device.createRfcommSocketToServiceRecord(SERVER_UUID);
		            Log.e(appName,"Make Connection..."+device.getName());
//		            Toast.makeText(getApplicationContext(), "Make Connection ", Toast.LENGTH_SHORT).show();
		        
			    	// Cancel discovery because it will slow down the connection
		            debug += "Canceling Discovering..";
//		            if(mBluetoothAdapter.isDiscovering())mBluetoothAdapter.cancelDiscovery();
//		            Log.e(appName,"Cancel Discovering..");
		            debug += "Cancel Discovering..";
			        
		            // Connect the device through the socket. This will block
		            // until it succeeds or throws an exception
		            mmSocket.connect();
		            Log.e(appName,"Connected..");
		            debug += "Connected..";
			     
			        // Do work to manage the connection (in a separate thread)
			        Log.e(appName,"managing..");
			        debug+="managing..";
//			        String name = mmSocket.getRemoteDevice().getName();
			        Log.e(appName, "Connected With "+mmSocket);
		                
			        InputStream in = mmSocket.getInputStream();
			        String status = readLine(in);
			        if(status.equalsIgnoreCase(STATUS_MESSAGE)){
			        	reciveMessage(in);
			        }else{
			        	recievePicture(in);
			        }
			        mmSocket.close();
				} catch (IOException e) { e.printStackTrace(); }
				catch(Exception ex){ex.printStackTrace();}
			}
    		handlerDismissDialog.sendEmptyMessage(0);
		}
	}
}
