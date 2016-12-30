package de.selfmade.raspicon;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

 

public class Share extends Activity {
    /** Called when the activity is first created. */
	
	Socket socket = null;
	String C_IP = MainActivity.C_IP;
	int C_PORT = MainActivity.C_PORT;	
	DataOutputStream DOS = null;
	boolean vd_pressed = false;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share);
         
        Thread.setDefaultUncaughtExceptionHandler(new MyThreadUncaughtExceptionHandler());        
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        
        // Get intent, action and MIME type
        Bundle extras = getIntent().getExtras();
        String vtext = extras.getString(Intent.EXTRA_TEXT);
        
        //if (vtext.startsWith("https://youtu.be/"))
        //{
        	byte[] data = "".getBytes();
        	if (pubTrans.AudioOnly == 0){
        		data = ("yv_" + vtext).getBytes();}
        	else{
        		data = ("ya_" + vtext).getBytes();}
        	transferData(data);
        //}        	
        //else createToast("Share Not Available");
        connectButton();
    }
    
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
	    switch(keyCode){
	        
	    case KeyEvent.KEYCODE_VOLUME_DOWN:
	    	vd_pressed = true;
			return true;
			
	    case KeyEvent.KEYCODE_VOLUME_UP:
			return true;
	    }
	    return super.onKeyDown(keyCode, event);
	}
	
	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
	    switch(keyCode){
	    case KeyEvent.KEYCODE_VOLUME_DOWN:
	    	vd_pressed = false;
			return true;
			
	    case KeyEvent.KEYCODE_VOLUME_UP:
			return true;
	    }
	    return super.onKeyDown(keyCode, event);
	}
    
    @Override
    public void onBackPressed() {
		transferData("q".getBytes());
		finish();
    }
    
	void connectButton()
	{
		Button bt_pause=(Button)findViewById(R.id.btn_pause);
		Button bt_l=(Button)findViewById(R.id.btn_l);
		Button bt_r=(Button)findViewById(R.id.btn_r);
		Button bt_exit=(Button)findViewById(R.id.btn_exit);		
		Button bt_leiser=(Button)findViewById(R.id.btn_shareleiser);
		Button bt_lauter=(Button)findViewById(R.id.btn_sharelauter);
		
		bt_leiser.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				transferData("vd".getBytes());
			}});
		
		bt_lauter.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				transferData("vu".getBytes());
			}});
		
		bt_pause.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				transferData("p".getBytes());
			}});
		
		bt_l.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (!vd_pressed)
					transferData("l".getBytes());
				else if (vd_pressed)
					transferData("ll".getBytes());
			}});
		
		bt_r.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (!vd_pressed)
					transferData("r".getBytes());
				else if (vd_pressed)
					transferData("rr".getBytes());
			}});
		
		bt_exit.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				transferData("q".getBytes());
				dc();
				finish();
			}});
	}   

    
		
	void connect2Server()
	{
		try {
			if (socket!=null)
				socket.close();
			socket = new Socket(C_IP, C_PORT);
			DOS = new DataOutputStream(socket.getOutputStream());
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
    
	void transferData(byte[] data)
	{
		connect2Server();
		try {    		
    			DOS.write(data);
		} catch (IOException e) {
			Log.d("nothing", "Just nothing");
		}
		dc();
	}
    
	void dc()
	{
		try {
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	void createToast(String text)
	{
		Context context = getApplicationContext();
		Toast toast = Toast.makeText(context, text, Toast.LENGTH_SHORT);
		toast.show();
	}
}