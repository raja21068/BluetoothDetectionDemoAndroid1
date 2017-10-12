package com.example.bluetoothexample;

import java.util.ArrayList;
import java.util.UUID;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

	protected static final int DISCOVERY_REQUEST = 1;
	ListView list;
	ArrayAdapter<String> mNewDevicesArrayAdapter;
	BluetoothAdapter mBluetoothAdapter;
	UUID recieverUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
	UUID senderUUID = UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66");
	EditText addressText, messageText ;
	Button buttonSend , buttonRecieve, buttonCancel ;
	TextView status;
	
	ArrayList<BluetoothDevice> devices = new ArrayList<BluetoothDevice>();
	BluetoothSocket socket;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		addressText = (EditText)findViewById(R.id.editTextAddress);
		buttonSend = (Button)findViewById(R.id.buttonSend);
		buttonRecieve = (Button)findViewById(R.id.buttonRecieve);
		messageText = (EditText)findViewById(R.id.editTextMessage);
		status = (TextView)findViewById(R.id.textViewStatus);
		buttonCancel = (Button)findViewById(R.id.buttonCancel);
		
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

		Button detect = (Button)findViewById(R.id.buttonDetect);
		list = (ListView)findViewById(R.id.listViewDevies);
		
		mNewDevicesArrayAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1); 
		
		refreshAdapter();
		ListView lv1 = (ListView) findViewById(R.id.listViewDevies);
		lv1.setAdapter(mNewDevicesArrayAdapter);
		
		
		detect.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				//refreshAdapter();
				
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
			}
		});
	
		setListListener();
		sendListener();
		receiveListener();
		cancelListener();
	}

	
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	public void refreshAdapter(){
		mNewDevicesArrayAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1); 	
	}
	
	private void setListListener(){
		list.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int postion,
					long arg3) {
				
				//Toast.makeText(getApplicationContext(), ""+list.getItemAtPosition(postion).toString(), Toast.LENGTH_SHORT).show();			
				String str = list.getItemAtPosition(postion).toString();
				addressText.setText(""+str.split("\n")[1].trim());
			}
		});
	}

	private void sendListener(){
		buttonSend.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(!addressText.getText().toString().isEmpty()){
					Toast.makeText(getApplicationContext(), "Started sending", Toast.LENGTH_SHORT).show();
					new SendData(mBluetoothAdapter, senderUUID, addressText.getText().toString(), getBaseContext(), getResources(), messageText.getText().toString(),status).execute();
				}
			}
		});
	}
	
	private void receiveListener(){
		buttonRecieve.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				Toast.makeText(getApplicationContext(), "Started recieving", Toast.LENGTH_SHORT).show();
				new AcceptData(mBluetoothAdapter, recieverUUID, getBaseContext(),status).execute();
				
				
			}
		});
	}
	
	private void cancelListener(){
		buttonCancel.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(mBluetoothAdapter.isDiscovering())mBluetoothAdapter.cancelDiscovery();
			}
		});
	}
}

