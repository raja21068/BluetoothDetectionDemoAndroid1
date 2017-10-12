package com.example.bluetoothtouchtransfer;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.UUID;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;

public class MainActivity extends Activity {

	public static UUID SERVER_UUID = UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66");
	public static UUID CLIENT_UUID = UUID.fromString("fa87c0d0-afac-111e-1a39-1811211c9a22");
	
	String message = "";
	String appName = "";
	
	BluetoothAdapter bluetoothAdpater;
	
	ArrayList<BluetoothDevice> devices = new ArrayList<BluetoothDevice>();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		bluetoothAdpater = BluetoothAdapter.getDefaultAdapter();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	
	private class BluetoothServer extends AsyncTask<Void, Void, Void>{
		
		BluetoothAdapter mBluetoothAdapter;
		BluetoothServerSocket mmServerSocket;
		
		public BluetoothServer(BluetoothAdapter mBluetoothAdapter) {
			this.mBluetoothAdapter = mBluetoothAdapter;
		}
		
		@Override
		protected Void doInBackground(Void... params) {
			BluetoothSocket socket = null;
	        // Keep listening until exception occurs or a socket is returned
	       while (true) {
	        	try {
	        		// MY_UUID is the app's UUID string, also used by the client code
	        		mmServerSocket = mBluetoothAdapter.listenUsingRfcommWithServiceRecord(appName, SERVER_UUID);
		            
//		            Log.e(appName, "Listening..");
	            	socket = mmServerSocket.accept();
	            	String name = socket.getRemoteDevice().getName();
//	            	Log.e(appName, "Connected With "+socket);
	                InputStream in = socket.getInputStream();
                	
                	message = name+": "+readLine(in);
                	
                	mmServerSocket.close();
                	//Log.e(appName, "Connection Closed");
	        	} catch (IOException e) {
	            	//Log.e(appName,">> "+ e.getMessage());
	            	continue;
	            	//break;
	            }catch (Exception e) {
	            	//Log.e(appName,">> "+ e.getMessage());
				}
	            
	        }
			//return null;
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

}
