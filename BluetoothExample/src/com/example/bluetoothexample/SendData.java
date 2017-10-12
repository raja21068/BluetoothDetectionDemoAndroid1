package com.example.bluetoothexample;

import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.widget.TextView;

public class SendData extends AsyncTask<Void, Void, Void> {

	private BluetoothDevice device = null;
	private BluetoothSocket btSocket = null;
	private OutputStream outStream = null;
	private BluetoothAdapter mBluetoothAdapter;
	private String address;
	private UUID myUUID;
	private Resources resources;
	private Context appContext;
	private String message;
	private TextView status;
	private String debug="";
	
	public SendData(BluetoothAdapter mBluetoothAdapter, UUID myUUID , String address, Context appContext, Resources resources , String message, TextView status){
		this.mBluetoothAdapter = mBluetoothAdapter;
		this.address = address;
		this.resources = resources;
		this.appContext = appContext;
		this.myUUID = myUUID;
		this.message = message;
		this.status = status;
	 }
	
	@Override
	protected void onPostExecute(Void result) {
		// TODO Auto-generated method stub
		super.onPostExecute(result);
		status.setText(debug);
	}
	
	@Override
	protected Void doInBackground(Void... params) {
		// TODO Auto-generated method stub
	try{	
		debug += (address.trim()+",");
		device = mBluetoothAdapter.getRemoteDevice(address.trim());
		try{
			btSocket = device.createRfcommSocketToServiceRecord(myUUID);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		debug+= "Start Connecting,";
		System.out.println("Start Connecting,");
		
		if(mBluetoothAdapter.isDiscovering())mBluetoothAdapter.cancelDiscovery();
		try {
			btSocket.connect();
			System.out.println(">>Connected...");
			debug+= "Connected";
			
		} catch (IOException e) {
			e.printStackTrace();
			try {
				btSocket.close();
				debug+= "Socket Closed";
				System.out.println("Socket Closed,");
			} catch (IOException e2) {e2.printStackTrace();}
		}
		//Toast.makeText(appContext, "Connected to " + device.getName(), Toast.LENGTH_SHORT).show();
		try {
			outStream = btSocket.getOutputStream();
			debug+= "Got output,";
			
		} catch (IOException e) {e.printStackTrace();}
		
		sendMessage();
	}catch(Exception ex){}	
		return null;
	
	}
	 
	 public void sendMessage(){
		 try {
			 mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
			 
			 /*Bitmap bm = BitmapFactory.decodeResource(resources, R.drawable.white);
			 ByteArrayOutputStream baos = new ByteArrayOutputStream();
			 bm.compress(Bitmap.CompressFormat.JPEG, 100,baos); //bm is the bitmap object
			 byte[] b = baos.toByteArray();
			 */
			 byte[] b = message.getBytes();
			 
			 //Toast.makeText(appContext, String.valueOf(b.length), Toast.LENGTH_SHORT).show();
			 outStream.write(b);
			 outStream.flush();
			 debug +="flushed";
		 } catch (IOException e) {e.printStackTrace();}
	 }
	 
}
