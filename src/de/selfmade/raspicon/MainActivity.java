package de.selfmade.raspicon;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import de.selfmade.raspicon.R;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

class MyThreadUncaughtExceptionHandler implements Thread.UncaughtExceptionHandler {
	
    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
    	//System.exit(0);
    }
}

public class MainActivity extends Activity {

	Socket socket = null;
	public static String C_IP = "192.168.1.101";
	public static int C_PORT = 5563;
	
	DataOutputStream DOS = null;
	BufferedReader DIS = null;
	long lastDown = 0;
	long voluphold = 0;
	long volupholdur = 0;
	long lastDuration = 0;	
	boolean vd_pressed = false;
	
	Spinner spn_files = null;
	Spinner spn_folders = null;
	String currentfolder = "";
	private Boolean firstspinnercall = true;
	int currentactivity = 0;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);	
                
        Thread.setDefaultUncaughtExceptionHandler(new MyThreadUncaughtExceptionHandler());
        
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.activity_main); 
		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		currentactivity = 0;
		if (getIntent().getExtras() != null) {
            Bundle bundle = getIntent().getExtras();
            currentfolder = bundle.getString("currentfolder");
        }
		simpleButtons();
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
	    switch(keyCode){
	    case KeyEvent.KEYCODE_BACK:
	    	Intent i=new Intent(this, MainActivity.class);
	        i.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
	        Bundle bundle = new Bundle();
	        i.putExtra("currentfolder", currentfolder);
	        startActivity(i);

	        return true;
	        
	    case KeyEvent.KEYCODE_MENU:
	    	if (currentactivity == 1)
	    		createToast(currentfolder);
	    	return true;
	        
	    case KeyEvent.KEYCODE_VOLUME_DOWN:
	    	vd_pressed = true;
			return true;
			
	    case KeyEvent.KEYCODE_VOLUME_UP:
	    	if (volupholdur < 999999)
	    	{
	    	voluphold = System.currentTimeMillis();
	    	volupholdur = 10000000; 
	    	}
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
	    	volupholdur = System.currentTimeMillis() - voluphold;
	           if (volupholdur > 3000)
	           {
	           
	    	final EditText txtUrl = new EditText(this);

	    	// Set the default text to a link of the Queen
	    	txtUrl.setText("192.168.1.");

	    	new AlertDialog.Builder(this)
	    	  .setTitle("Set IP")
	    	  .setMessage("Set IP of the Raspberry PI")
	    	  .setView(txtUrl)
	    	  .setPositiveButton("OK", new DialogInterface.OnClickListener() {
	    	    public void onClick(DialogInterface dialog, int whichButton) {
	    	    	C_IP = txtUrl.getText().toString();
	    	    }
	    	  })
	    	  .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
	    	    public void onClick(DialogInterface dialog, int whichButton) {
	    	    }
	    	  })
	    	  .show();
	           }
			return true;
	    }
	    return super.onKeyDown(keyCode, event);
	}
	
	List<String> createList(String[] list)
	{
		List<String> liste = new ArrayList<String>();
		for( String k: list )
		{
			liste.add(k);
		}
		return liste;
	}
	
	@SuppressLint("ClickableViewAccessibility")
	void simpleButtons()
	{
		Button btn_restart=(Button)findViewById(R.id.btn_restart);
		Button btn_shutdown=(Button)findViewById(R.id.btn_shutdown);
		Button btn_playradio=(Button)findViewById(R.id.btn_playradio);
		Button btn_volumeup=(Button)findViewById(R.id.btn_volumeup);
		Button btn_volumedown=(Button)findViewById(R.id.btn_volumedown);
		Button btn_mainpause=(Button)findViewById(R.id.btn_mainpause);
		Button btn_mainforward=(Button)findViewById(R.id.btn_mainforward);
		Button btn_mainback=(Button)findViewById(R.id.btn_mainback);
		Button btn_playstream=(Button)findViewById(R.id.btn_playstream);
		Button btn_closeall=(Button)findViewById(R.id.btn_closeall);
		Button btn_filebrowser=(Button)findViewById(R.id.btn_filebrowser);
		Switch sw_useyoutubedl = (Switch) findViewById(R.id.sw_useyoutubedl);
		final EditText txt_streamnumber =(EditText)findViewById(R.id.txt_streamnumber);
		final Spinner spn_radios=(Spinner)findViewById(R.id.sp_radio);
		final Spinner spn_stream=(Spinner)findViewById(R.id.sp_stream);
		
		final String radios[] = {"http://xapp2023227392c40000.f.l.i.lb.core-cdn.net/40000mb/live/app2023227392/w2179104775/live_de_128.mp3"
				, "http://62.138.144.38:8040"
				, "http://mp3.planetradio.de/plrchannels/hqitunes.mp3"
				, "http://mp3.ffh.de/ffhchannels/hqeurodance.mp3"
				, "http://regiocast.hoerradar.de/80s80s-event01-mp3-mq"
				, "http://88.99.57.167/christmas"
				, "http://ndr-ndrloop8-hi-mp3.akacast.akamaistream.net/7/762/160823/v1/gnl.akacast.akamaistream.net/ndr_ndrloop8_hi_mp3"
				, "http://stream01.iloveradio.de/iloveradio1.mp3"
				, "http://streams.rpr1.de/rpr-evergreens-128-mp3?usid=0-0-H-M-D-07"};
		String radiolist[] = {"iTunes T40","Radio T40","NDR T100","I Love Radio", "Eurodance", "Christmas 1", "Christmas 2", "Christmas 3", "Christmas 4"};
		String streams[] = {"DB", "OP"};
		List<String> listfo = createList(radiolist);
		ArrayAdapter<String> dataAdapterfo = new ArrayAdapter<String>(getApplicationContext() ,R.layout.style_spinner, listfo);
		dataAdapterfo.setDropDownViewResource(R.layout.style_spinner);
		spn_radios.setAdapter(dataAdapterfo);
		
		listfo = createList(streams);
		dataAdapterfo = new ArrayAdapter<String>(getApplicationContext() ,R.layout.style_spinner, listfo);
		dataAdapterfo.setDropDownViewResource(R.layout.style_spinner);
		spn_stream.setAdapter(dataAdapterfo);		

		sw_useyoutubedl.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (isChecked) {
					transferData("cr0".getBytes());
				} else {
					transferData("cr1".getBytes());
				}
			}
		});
		
		btn_closeall.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				transferData("q".getBytes());
			}});
		
		btn_playradio.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				//connect2Server();
				if (spn_radios.getSelectedItem().toString() == "Christmas 2")					
					transferData(("dl_" + radios[4]).getBytes());
				else if (spn_radios.getSelectedItem().toString() == "Radio T40")					
					transferData(("dl_" + radios[0]).getBytes());
				else if (spn_radios.getSelectedItem().toString() == "Christmas 1")					
					transferData(("dl_" + radios[1]).getBytes());
				else if (spn_radios.getSelectedItem().toString() == "Christmas 3")					
					transferData(("dl_" + radios[5]).getBytes());
				else if (spn_radios.getSelectedItem().toString() == "iTunes T40")					
					transferData(("dl_" + radios[2]).getBytes());
				else if (spn_radios.getSelectedItem().toString() == "Eurodance")					
					transferData(("dl_" + radios[3]).getBytes());
				else if (spn_radios.getSelectedItem().toString() == "NDR T100")					
					transferData(("dl_" + radios[6]).getBytes());
				else if (spn_radios.getSelectedItem().toString() == "I Love Radio")					
					transferData(("dl_" + radios[7]).getBytes());
				else if (spn_radios.getSelectedItem().toString() == "Christmas 4")					
					transferData(("dl_" + radios[8]).getBytes());
				//dc();
			}});
		
		btn_restart.setOnTouchListener(new View.OnTouchListener() {			
			@Override
		     public boolean onTouch(View v, MotionEvent event) {

				
		        if(event.getAction() == MotionEvent.ACTION_DOWN) {
		           lastDown = System.currentTimeMillis();
		        } else if (event.getAction() == MotionEvent.ACTION_UP) {
		           lastDuration = System.currentTimeMillis() - lastDown;
		           if (lastDuration > 3000)
		           {
		        	   transferData("gr".getBytes());
		           }
		        }
		        return true;
		     }
			});
		
		
		btn_shutdown.setOnTouchListener(new View.OnTouchListener() {
			@SuppressLint("ClickableViewAccessibility")
			@Override
		     public boolean onTouch(View v, MotionEvent event) {

				
		        if(event.getAction() == MotionEvent.ACTION_DOWN) {
		           lastDown = System.currentTimeMillis();
		        } else if (event.getAction() == MotionEvent.ACTION_UP) {
		           lastDuration = System.currentTimeMillis() - lastDown;
		           if (lastDuration > 3000)
		           {
		        	   transferData("gd".getBytes());
		           }
		        }
		        return true;
		     }
			});		

		
		btn_playstream.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String command = "";
				if (spn_stream.getSelectedItem().toString() == "OP")					
					command = "str_op" + txt_streamnumber.getText().toString();
				else if (spn_stream.getSelectedItem().toString() == "DB")
					command = "str_db" + txt_streamnumber.getText().toString();
				transferData(command.getBytes());
			}});
		
		btn_volumedown.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				transferData("vd".getBytes());
			}});
		
		btn_mainpause.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				transferData("p".getBytes());
			}});
		
		btn_filebrowser.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				FileBrowser();
			}});
		
		btn_mainforward.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (!vd_pressed)
					transferData("r".getBytes());
				else if (vd_pressed)
					transferData("rr".getBytes());
			}});
		btn_mainback.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (!vd_pressed)
					transferData("l".getBytes());
				else if (vd_pressed)
					transferData("ll".getBytes());
			}});
		
		btn_volumeup.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				transferData("vu".getBytes());
			}});
	}
	
	void FileBrowser()
	{
		setContentView(R.layout.activity_filebrowser);
		currentactivity = 1;
		spn_files=(Spinner)findViewById(R.id.sp_files);
		spn_folders=(Spinner)findViewById(R.id.sp_folders);
		Button btn_playfile=(Button)findViewById(R.id.btn_playfile);
		Button btn_folderback=(Button)findViewById(R.id.btn_folderback);
		Button btn_folderreload=(Button)findViewById(R.id.btn_folderreload);
		if (currentfolder == "")
			getFolderFiles("%%home%%");
		else
			getFolderFiles(currentfolder);
		
		btn_playfile.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (spn_files.getSelectedItem().toString() != "No Files")
				{	
					String command = "fs_" + currentfolder + spn_files.getSelectedItem().toString();
					transferData(command.getBytes());
				}
			}});
		
		btn_folderreload.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				getFolderFiles(currentfolder);
			}});
		
		btn_folderback.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String backfolder = "/";
				String folders[] = currentfolder.split("\\/");
				for (int i = 1; i<folders.length - 1; i++)
					backfolder += folders[i] + "/";
				getFolderFiles(backfolder);
			}});
		
		spn_folders.setOnItemSelectedListener(new OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1,int arg2, long arg3) {
            	boolean gonein = false;
            	if (!firstspinnercall && spn_folders.getSelectedItem().toString() != "No Folders")
            	{
                String folder = spn_folders.getSelectedItem().toString();
                getFolderFiles(currentfolder + folder + "/");
                gonein = true;
            	}
            	if (!gonein)
            		firstspinnercall = false;
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                // TODO Auto-generated method stub

            }
        });
	}
	
	
	@SuppressWarnings("null")
	void getFolderFiles(String dir)
	{
		firstspinnercall = true;
		String command = "rd_" + dir;
		String folder = transfer_readData(command.getBytes());
		
		String content[] = folder.split("\\%\\%\\/\\%\\%");
		List<String> listfiles = new ArrayList<String>();
		List<String> listfolders = new ArrayList<String>();
		for(String i1: content)
		{
			
			if (i1.startsWith("c"))
			{
				currentfolder = i1.substring(1);
			}
			else if (i1.startsWith("d"))
			{
				listfolders.add(i1.substring(1));
			}
			else if (i1.startsWith("f"))
			{
				listfiles.add(i1.substring(1));
			}
		}
		if (listfiles.toArray().length == 0)
			listfiles.add("No Files");
		if (listfolders.toArray().length == 0)
			listfolders.add("No Folders");
		ArrayAdapter<String> dataAdapterfo = new ArrayAdapter<String>(getApplicationContext() ,R.layout.style_spinner, listfiles);
		dataAdapterfo.setDropDownViewResource(R.layout.style_spinner);
		spn_files.setAdapter(dataAdapterfo);
		
		dataAdapterfo = new ArrayAdapter<String>(getApplicationContext() ,R.layout.style_spinner, listfolders);
		dataAdapterfo.setDropDownViewResource(R.layout.style_spinner);
		spn_folders.setAdapter(dataAdapterfo);

	}
	
	void transferData(byte[] data)
	{
		connect2Server();
		try {    		
    			DOS.write(data);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			Log.d("nothing", "Just nothing");
		}
		dc();
	}
	
	String transfer_readData(byte[] data)
	{
		connect2Server();
		try {    		
    			DOS.write(data);
    			BufferedReader DIS = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    			String readdata = DIS.readLine();
    			dc();
    			return readdata;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			Log.d("nothing", "Just nothing");
		}
		dc();
		return null;
	}
	
	void connect2Server()
	{
		try {
			if (socket!=null)
				socket.close();
			//socket.close();
			socket = new Socket(C_IP, C_PORT);
			DOS = new DataOutputStream(socket.getOutputStream());

			//DOS.writeUTF("HELLO_WORLD");
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			Log.d("nothing", "Just nothing");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			Log.d("nothing", "Just nothing");
		}
	}
	
	void createToast(String text)
	{
		Context context = getApplicationContext();
		Toast toast = Toast.makeText(context, text, Toast.LENGTH_SHORT);
		toast.show();
	}
	
	void dc()
	{
		try {
			socket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
