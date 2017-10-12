package com.example.bluetoothserver;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements OnClickListener{
	public static String appName = "BluetoothServer";
	TextView statusView;
	UUID recieverUUID = UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66");	
	String debug=""; 
	String message = "";
	AcceptThread acceptThread;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		statusView = (TextView)findViewById(R.id.textViewStatus);
		
		acceptThread = new AcceptThread(BluetoothAdapter.getDefaultAdapter(), recieverUUID);
		acceptThread.start();
	}

	@Override
	protected void onResume() {
		Toast.makeText(getApplicationContext(), "Resumed", Toast.LENGTH_SHORT).show();
		super.onResume();	
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	


	@Override
	public void onClick(View v) {
		
	}
	
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
	            	String name = socket.getRemoteDevice().getName();
	            	Log.e(appName, "Connected With "+socket);
	                InputStream in = socket.getInputStream();
                	
                	message = name+": "+readLine(in);
                	handler.sendEmptyMessage(0);

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

	Handler handler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			setStatus(message);
			debug = "";
		}
	};
	

	void setStatus(String status){
		String s = statusView.getText().toString();
		s+= ("\n"+status);
		statusView.setText(s);
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
}
