package com.example.bluetoothclient;

import java.io.File;

import android.app.Dialog;
import android.content.Context;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

public class ListDialog extends Dialog implements OnClickListener, OnItemClickListener{

	ListView list;
	Button buttonBack;
	ArrayAdapter<String> adapter;
	File directory;
	StringBuilder path ;
	Context context;
	String[] str ;
	MainActivity parent;
	public ListDialog(Context conext, MainActivity main) {
		super(conext);
		this.context = conext;
		this.parent= main;
		setContentView(R.layout.dialog_files_list);
		list = (ListView)findViewById(R.id.listViewFiles);
		buttonBack = (Button)findViewById(R.id.buttonBack);
		list.setOnItemClickListener(this);
		directory = new File(Environment.getExternalStorageDirectory(),"");
		str = directory.list();
	}
	
	@Override
	public void show() {
		Log.e("ListDialog","how Called");
		path = new StringBuilder("");
		adapter = new ArrayAdapter(getContext(),android.R.layout.simple_list_item_1);
		list.setAdapter(adapter);
		for(int i=0;i<str.length;i++){
			adapter.add(str[i]);
		}
		super.show();
	}
	
	@Override
	public void onClick(View view){
		if(view == buttonBack){
			dismiss();
		}
	}
	
	@Override
	public void onItemClick(AdapterView<?> arg0, View view, int index, long arg3) {
		Log.e("ListDialog", "Path: "+path);
//		path.append(list.getItemAtPosition(index).toString());
//		File f = new File(directory , path.toString());
//		if(f.isDirectory()){
//			Log.e("ListDialog", "This is directory");
//			adapter = new ArrayAdapter(getContext(),android.R.layout.simple_list_item_1);
//			list.setAdapter(adapter);
//			String strs[] = f.list();
//			for(int i=0;i<str.length;i++){
//				adapter.add(str[i]);
//			}
//		}
//		else{
			String path = list.getItemAtPosition(index).toString();
			String str = path.toString();
			Log.e("Str", ""+str);
			str = str.substring(str.lastIndexOf(".")+1 , str.length());
			if( str.equalsIgnoreCase("jpeg") || str.equalsIgnoreCase("png")|| str.equalsIgnoreCase("jpg")){
				Log.e("Path",directory.getAbsolutePath()+File.separator+path.toString());
				parent.setImage(directory.getAbsolutePath()+File.separator+path.toString());
				dismiss();
			}else{
				Toast.makeText(context, "File must be image", Toast.LENGTH_SHORT).show();
			}
//		}
	}
	
}
