package com.pokebros.android.pokemononline;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.regex.Pattern;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.pokebros.android.pokemononline.RegistryConnectionService.RegistryCommandListener;
import com.pokebros.android.pokemononline.ServerListAdapter.Server;
import com.pokebros.android.pokemononline.player.FullPlayerInfo;

public class RegistryActivity extends Activity implements ServiceConnection, RegistryCommandListener {
	
	static final String TAG = "RegistryActivity";
	
	private ServerListAdapter adapter;
	private EditText editAddr;
	private EditText editName;
	private boolean bound = false;
	private String path;
	private FullPlayerInfo meLoginPlayer;
	RegistryConnectionService service;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // If we are already connected to a server show ChatActivity instead of RegistryActivity
        if (!getIntent().hasExtra("sticky")) {
	        ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
	        for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
	            if ("com.pokebros.android.pokemononline.NetworkService".equals(service.service.getClassName())) {
					startActivity(new Intent(RegistryActivity.this, ChatActivity.class));
	            	finish();
	            	return;
	            }
	        }
        }
        if (getIntent().hasExtra("failedConnect")) {
        	Toast.makeText(this, "Server connection failed", Toast.LENGTH_LONG).show();
        }
        
        this.stopService(new Intent(RegistryActivity.this, NetworkService.class));
        
        setContentView(R.layout.main);
         
		editAddr = (EditText)RegistryActivity.this.findViewById(R.id.addredit);
		editName = (EditText)RegistryActivity.this.findViewById(R.id.nameedit);
		// Hide the soft-keyboard when the activity is created
		editName.setInputType(InputType.TYPE_NULL);
		editName.setOnTouchListener(new View.OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				editName.setInputType(InputType.TYPE_CLASS_TEXT);
				editName.onTouchEvent(event);
				return true;
			}
		});

		meLoginPlayer = new FullPlayerInfo(RegistryActivity.this);
		editName.append(meLoginPlayer.nick());
		
		//Capture out button from layout
        Button conbutton = (Button)findViewById(R.id.connectbutton);
        Button importbutton = (Button)findViewById(R.id.importteambutton);
        //Register onClick listener
        conbutton.setOnClickListener(registryListener);
        importbutton.setOnClickListener(registryListener);
        
        ListView servers = (ListView)findViewById(R.id.serverlisting);
        adapter = new ServerListAdapter(this, R.id.serverlisting);
        servers.setAdapter(adapter);
        servers.setOnItemClickListener(new OnItemClickListener() {
        	/* Set the edit texts on list item click */
			public void onItemClick(AdapterView<?> parent, View view, int position,
					long id) {
				Server server = (Server)parent.getItemAtPosition(position);
				editAddr.setText("");
				editAddr.append(server.ip + ":" + String.valueOf(server.port));
			}
		});
        
        
        servers.setOnItemLongClickListener(new OnItemLongClickListener() {

			public boolean onItemLongClick(AdapterView<?> parent, View view,
					int position, long id) {
				Server server = (Server)parent.getItemAtPosition(position);
				Log.e(TAG, "Long click works: " + server.desc);
				// TODO: Display server description
		        /*Intent intent = new Intent(RegistryActivity.this, RichTextActivity.class);
		        intent.putExtra("richtext", server.desc);
		        RegistryActivity.this.startActivity(intent); */
				return true;
			}
        	
		});
        
        Intent intent = new Intent(RegistryActivity.this, RegistryConnectionService.class);
        bindService(intent, this, 0);
        startService(intent);
    }
    
    private OnClickListener registryListener = new OnClickListener() {
    	public void onClick(View v) {
    		if (v == findViewById(R.id.connectbutton)){
    			String ipString = editAddr.getText().toString().split(":")[0];
    			String portString = "";
    			try {
    				portString = editAddr.getText().toString().split(":")[1];
    			} catch (ArrayIndexOutOfBoundsException e) {
					// No need to act
    			}
    			short portVal = -1;
				try {
					portVal = Short.parseShort(portString);
				} catch(NumberFormatException e) {
					// No need to act
				}
				if (portVal < 1 || portVal > 65535) {
					// TODO: R.string
					Toast.makeText(RegistryActivity.this, "Invalid value for port", Toast.LENGTH_LONG).show();
        			return;
				}
				
				String nick = editName.getText().toString();
				if (nick.length() > 0 && !nick.equals(meLoginPlayer.nick())) {
					// Save name changes
					try {
						// Open team for reading
						FileInputStream team = RegistryActivity.this.openFileInput("team.xml");
						
						// Read team into ByteArrayOutputStream
						Baos saveBuffer = new Baos();
						byte[] buffer = new byte[1024];
						int length;
						while ((length = team.read(buffer))>0)
							saveBuffer.write(buffer, 0, length);
						team.close();
						
						// Replace trainer name in Baos with user entered trainer name
						String stringBuffer = new String(saveBuffer.toByteArray());
						stringBuffer = stringBuffer.replaceFirst(">.*</Trainer>", ">" + nick + "</Trainer>");
						
						// Write Baos to file
						FileOutputStream saveTeam = openFileOutput("team.xml", Context.MODE_PRIVATE);
						saveTeam.write(stringBuffer.getBytes());
						saveTeam.flush();
						saveTeam.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} finally {
						meLoginPlayer.playerTeam.nick = nick;
					}
				}
				
				Intent intent = new Intent(RegistryActivity.this, NetworkService.class);
				intent.putExtra("ip", ipString);
				intent.putExtra("port", portVal);
				Bundle loginPlayer = new Bundle();
				loginPlayer.putByteArray("loginBytes", meLoginPlayer.serializeBytes().toByteArray());
				intent.putExtra("loginPlayer", loginPlayer);

				startService(intent);
				startActivity(new Intent(RegistryActivity.this, ChatActivity.class));
				RegistryActivity.this.finish();
    		}
    		else if (v == findViewById(R.id.importteambutton)) {
				AlertDialog.Builder alert = new AlertDialog.Builder(RegistryActivity.this);

				alert.setTitle("Team Import");
				alert.setMessage("Please type the path to your team.");

				// Set an EditText view to get user input
				final EditText input = new EditText(RegistryActivity.this);
				input.append("/sdcard/");
				alert.setView(input);

				alert.setPositiveButton("Import",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,	int whichButton) {
								String path = input.getText().toString();
								
								if (path != null) {
									try {
										// Copy imported file to default team location
										FileOutputStream saveTeam = openFileOutput("team.xml", Context.MODE_PRIVATE);
										FileInputStream team = new FileInputStream(path);

										byte[] buffer = new byte[1024];
										int length;
										while ((length = team.read(buffer))>0)
											saveTeam.write(buffer, 0, length);
										saveTeam.flush();
										saveTeam.close();
										team.close();
									} catch (IOException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
								}
								
								meLoginPlayer = new FullPlayerInfo(RegistryActivity.this);
								editName.setText("");
								editName.append(meLoginPlayer.nick());
								Toast.makeText(getApplicationContext(), "Team imported from " + path, Toast.LENGTH_SHORT).show();
						}});

				alert.setNegativeButton("Cancel",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,	int whichButton) {
						}
					});

				alert.show();
    		}
    	}
    };
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.mainoptions, menu);
        return true;
    }
    
	public void ServerListEnd() {
		runOnUiThread(new Runnable() {
			public void run() {
				adapter.sortByPlayers();
				service.stopSelf();
			}
		});
	}

	public void NewServer(final String name, final String desc, final short players,
			final String ip, final short maxplayers, final short port) {
		runOnUiThread(new Runnable() {
			public void run() {
            	adapter.addServer(name, desc, ip, port, players, maxplayers);		
			}
		});
	}

	public void onServiceConnected(ComponentName name, IBinder binder) {
		bound = true;
		service = ((RegistryConnectionService.LocalBinder)binder).getService();
		service.setListener(this);
	}
	
	public void printToast(final String s, final int len) {
		runOnUiThread(new Runnable() {
			public void run() {
				Toast.makeText(RegistryActivity.this, s, len).show();
			}
		});
	}

	public void onServiceDisconnected(ComponentName name) {
		bound = false;
		unbindService(this);
		if (service != null)
	    	service.setListener(null);
	}
    
    @Override
    public void onDestroy() {
    	if (bound)
    		unbindService(this);
    	super.onDestroy();
    }
}