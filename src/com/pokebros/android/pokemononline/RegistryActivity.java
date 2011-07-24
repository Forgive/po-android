package com.pokebros.android.pokemononline;

import java.io.IOException;

import org.xmlpull.v1.XmlPullParserException;

import com.pokebros.android.pokemononline.RegistryConnectionService.RegistryCommandListener;
import com.pokebros.android.pokemononline.ServerListAdapter.Server;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

public class RegistryActivity extends Activity implements ServiceConnection, RegistryCommandListener {
	
	static final String TAG = "RegistryActivity";
	
	private ServerListAdapter adapter;
	private EditText ip;
	private EditText port;
	
	RegistryConnectionService service;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
         
		ip = (EditText)RegistryActivity.this.findViewById(R.id.ipedit);
		port = (EditText)RegistryActivity.this.findViewById(R.id.portedit);
		ip.setText("141.212.112.54");
		port.setText("5080");
		
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
				ip.setText(server.ip);
				port.setText(String.valueOf(server.port));
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
        Log.v(TAG, "Service started!");
        
    }
    
    private OnClickListener registryListener = new OnClickListener() {
    	public void onClick(View v) {
    		if (v == findViewById(R.id.connectbutton)){
    			short portVal = -1;
				try {
					portVal = Short.parseShort(port.getText().toString());
				} catch(NumberFormatException e) {
					// No need to act
				}
				if (portVal < 1 || portVal > 65535) {
					// TODO: R.string
					Toast.makeText(RegistryActivity.this, "Invalid value for port", Toast.LENGTH_LONG);
        			return;
				}

				Intent intent = new Intent(RegistryActivity.this, NetworkService.class);
				intent.putExtra("ip", ip.getText().toString());
				intent.putExtra("port", portVal);
				startService(intent);
				startActivity(new Intent(RegistryActivity.this, ChatActivity.class));
    		}
    		else if (v == findViewById(R.id.importteambutton)) {
					try {
						PokeParser p = new PokeParser();
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
    			Toast.makeText(getApplicationContext(), "You are super cool!", Toast.LENGTH_SHORT).show();
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
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        	Toast.makeText(this, "You pressed the icon!", Toast.LENGTH_LONG).show();
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
		service = ((RegistryConnectionService.LocalBinder)binder).getService();
		service.setListener(this);
	}
	
	public void onServiceDisconnected(ComponentName name) {
		if (service != null)
	    	service.setListener(null);
	}
    
    @Override
    public void onDestroy() {
    	unbindService(this);
    	super.onDestroy();
    }
}