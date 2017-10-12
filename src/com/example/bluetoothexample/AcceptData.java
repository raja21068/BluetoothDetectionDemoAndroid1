package com.example.bluetoothexample;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.AsyncTask;
import android.widget.TextView;
import android.widget.Toast;

public class AcceptData extends AsyncTask<Void, Void, Void>{
	private BluetoothServerSocket mmServerSocket;
	private BluetoothSocket socket = null;
	private InputStream mmInStream;
	private String device;
	private BluetoothAdapter mBluetoothAdapter;
	private UUID myUUID;
	private Context appContext;
	byte[] buffer = new byte[100];
	TextView status;
	String debug;
	
	public AcceptData(BluetoothAdapter mBluetoothAdapter, UUID myUUID, Context appContext, TextView statusView){
		this.mBluetoothAdapter = mBluetoothAdapter;
		this.myUUID = myUUID;
		this.appContext = appContext;
		this.status = statusView;
	}
	
	@Override
	protected void onPostExecute(Void result) {
		// TODO Auto-generated method stub
		super.onPostExecute(result);
		status.setText(debug);
	}
	
	@Override
	protected Void doInBackground(Void... params) {
		BluetoothServerSocket tmp = null;
		try {
			tmp = mBluetoothAdapter.listenUsingRfcommWithServiceRecord("Bluetooth", myUUID);
		} catch (IOException e) {
			e.printStackTrace();
		}
		//Toast.makeText(appContext, "Started recieving", Toast.LENGTH_SHORT).show();
		debug+="Waiting,";
		System.out.println(">>Waiting..");
		mmServerSocket = tmp;
		try {
			socket = mmServerSocket.accept();
			System.out.println(">>Connected...");
			debug+="Connected,";
		} catch (IOException e) {
			e.printStackTrace();
		}
		device = socket.getRemoteDevice().getName();
		debug+="got device,";
		InputStream tmpIn = null;
		try {
			tmpIn = socket.getInputStream();
			debug+="Got input,";
			System.out.println(">>Got input...");
		} catch (IOException e) {
			e.printStackTrace();
		}
		mmInStream = tmpIn;
		int byteNo;
		try {
			byteNo = mmInStream.read(buffer);
			debug+="read buffer,";
			if (byteNo != -1) {
				//ensure DATAMAXSIZE Byte is read.
				/*int byteNo2 = byteNo;
				int bufferSize = 7340;
				while(byteNo2 != bufferSize){
					bufferSize = bufferSize - byteNo2;
					byteNo2 = mmInStream.read(buffer,byteNo,bufferSize);
					if(byteNo2 == -1){
						break;
					}
					byteNo = byteNo+byteNo2;
				}*/
				String s = "";
				while(mmInStream.available()>0){
					byte[] buffer = new byte[100];
					mmInStream.read(buffer, 0, buffer.length);
					 s +=  (new String(buffer));
				}
				debug+=">"+s+"<";
				System.out.println(">>>>> "+s);
			}
			if (socket != null) {
				try {
					mmServerSocket.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
