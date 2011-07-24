package com.pokebros.android.pokemononline;

import com.pokebros.android.pokemononline.poke.BattlePoke;
import com.pokebros.android.pokemononline.poke.ShallowBattlePoke;

import de.marcreichelt.android.RealViewSwitcher;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.SystemClock;
import android.text.SpannableStringBuilder;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class BattleActivity extends Activity {
	public final static int SWIPE_TIME_THRESHOLD = 100;
	
	RealViewSwitcher realViewSwitcher;
	TextProgressBar[] hpBars = new TextProgressBar[2];
	int[] lastHPs = new int[2];
	TextView[] currentPokeNames = new TextView[2];
	public Button[] attack = new Button[4];
	public TextView[] timers = new TextView[2];
	TextView[] pokeListNames = new TextView[6];
	TextView[] pokeListHPs = new TextView[6];
	LinearLayout[] pokeListButtons = new LinearLayout[6];
	public TextView infoView;
	public ScrollView infoScroll;
	TextView[] names = new TextView[2];
	ImageView[] pokeSprites = new ImageView[2];
	private NetworkService netServ = null;
	int me, opp;
	
	 /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	System.out.println("BattleActivity Created");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.battle_pokeviewer);

        Intent intent = new Intent(BattleActivity.this, NetworkService.class);
        intent.putExtra("Type", "battle");
        bindService(intent, connection, Context.BIND_AUTO_CREATE);
        
        realViewSwitcher = (RealViewSwitcher)findViewById(R.id.battlePokeSwitcher);
        //Capture out button from layout
        attack[0] = (Button)findViewById(R.id.attack1);
        attack[1] = (Button)findViewById(R.id.attack2);
        attack[2] = (Button)findViewById(R.id.attack3);
        attack[3] = (Button)findViewById(R.id.attack4);
        
        pokeListNames[0] = (TextView)findViewById(R.id.pokename1);
        pokeListNames[1] = (TextView)findViewById(R.id.pokename2);
        pokeListNames[2] = (TextView)findViewById(R.id.pokename3);
        pokeListNames[3] = (TextView)findViewById(R.id.pokename4);
        pokeListNames[4] = (TextView)findViewById(R.id.pokename5);
        pokeListNames[5] = (TextView)findViewById(R.id.pokename6);

        pokeListHPs[0] = (TextView)findViewById(R.id.hp1);
        pokeListHPs[1] = (TextView)findViewById(R.id.hp2);
        pokeListHPs[2] = (TextView)findViewById(R.id.hp3);
        pokeListHPs[3] = (TextView)findViewById(R.id.hp4);
        pokeListHPs[4] = (TextView)findViewById(R.id.hp5);
        pokeListHPs[5] = (TextView)findViewById(R.id.hp6);
        
        pokeListButtons[0] = (LinearLayout)findViewById(R.id.pokeViewLayout1);
        pokeListButtons[1] = (LinearLayout)findViewById(R.id.pokeViewLayout2);
        pokeListButtons[2] = (LinearLayout)findViewById(R.id.pokeViewLayout3);
        pokeListButtons[3] = (LinearLayout)findViewById(R.id.pokeViewLayout4);
        pokeListButtons[4] = (LinearLayout)findViewById(R.id.pokeViewLayout5);
        pokeListButtons[5] = (LinearLayout)findViewById(R.id.pokeViewLayout6);
        
        infoView = (TextView)findViewById(R.id.infoWindow);
        infoScroll = (ScrollView)findViewById(R.id.infoScroll);
        //Register the onCLick listener with the implementation above
        for(int i = 0; i < 4; i++) {
        	attack[i].setOnClickListener(battleListener);
        }
        for(int i = 0; i < 6; i++)
        	pokeListButtons[i].setOnClickListener(battleListener);
    }
	
	private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
                changeName(msg);
        }
    };
    
	private Runnable updateTimeTask = new Runnable() {
		public void run() {
			for(int i = 0; i < 2; i++) {
				int seconds;
				if (netServ.battle.ticking[i]) {
					long millis = SystemClock.uptimeMillis()
					- netServ.battle.startingTime[i];
					seconds = netServ.battle.remainingTime[i] - (int) (millis / 1000);
				}
				else
					seconds = netServ.battle.remainingTime[i];

				if(seconds < 0) seconds = 0;
				else if(seconds > 300) seconds = 300;

				int minutes = (seconds / 60);
				seconds = seconds % 60;
				timers[i].setText(String.format("%02d:%02d", minutes, seconds));
			}
			handler.postDelayed(this, 200);
		}
	};
    
	void hpUpdateHack(int i) {
		hpBars[i].incrementProgressBy(1);
		hpBars[i].incrementProgressBy(-1);
	}
	
	public Runnable animateHPBars = new Runnable() {
		public void run() {
			for(int i = 0; i < 2; i++) {
				ShallowBattlePoke poke = netServ.battle.currentPoke(i);
				if(poke != null) {
					// Update the bars to reflect the current life percentage
					if(hpBars[i].getProgress() > poke.lifePercent)
						hpBars[i].incrementProgressBy(-1);
					else if(hpBars[i].getProgress() < poke.lifePercent) {
						hpBars[i].incrementProgressBy(1);
					}
					int progress = hpBars[i].getProgress();
					
					// Check to see if the bars need to change color, and do it
					Rect bounds = hpBars[i].getProgressDrawable().getBounds();
					if(progress > 50 && (lastHPs[i] <= 50)) {
						hpBars[i].setProgressDrawable(getResources().getDrawable(R.drawable.green_progress));
						hpUpdateHack(i);
					}
					else if((progress <= 50 && progress > 20) && (lastHPs[i] <= 20 || lastHPs[i] > 50)) {
						hpBars[i].setProgressDrawable(getResources().getDrawable(R.drawable.yellow_progress));
						hpUpdateHack(i);
					}
					else if((progress <= 20) && (lastHPs[i] > 20)) {
						hpBars[i].setProgressDrawable(getResources().getDrawable(R.drawable.red_progress));
						hpUpdateHack(i);
					}
					hpBars[i].getProgressDrawable().setBounds(bounds);
					
					// Update the percentage display on the hp bar
					hpBars[i].setText(progress + "%");
					lastHPs[i] = progress;
				}
			}
			// See if the animation has finished yet
			for(int i = 0; i < 2; i++) {
				ShallowBattlePoke poke = netServ.battle.currentPoke(i);
				if(poke != null) {
					if(hpBars[i].getProgress() != poke.lifePercent)
						handler.postDelayed(this, 100);
				}
			}
		}
	};
	
	public Runnable updateUITask = new Runnable() {
		public void run() {
			SpannableStringBuilder delta = netServ.battle.histDelta;
			infoView.append(delta);
			if (delta.length() != 0) {
		    	infoScroll.post(new Runnable() {
		    		public void run() {
		    			infoScroll.smoothScrollTo(0, infoView.getMeasuredHeight());
		    		}
		    	});
			}
			infoScroll.invalidate();
			
			if(netServ.battle.pokeChanged) {
				ShallowBattlePoke poke = netServ.battle.currentPoke(me);
				// Load correct moveset and name
				if(poke != null) {
					currentPokeNames[me].setText(netServ.battle.currentPoke(me).rnick());
					hpBars[me].setProgress(netServ.battle.currentPoke(me).lifePercent);
					BattlePoke battlePoke = netServ.battle.myTeam.pokes[0];
			        for(int i = 0; i < 4; i++) {
			        	attack[i].setText(battlePoke.moves[i].toString());
			        }
			        int resID = getResources().getIdentifier("p" + poke.uID.pokeNum + "_back",
			        		"drawable", "com.pokebros.android.pokemononline");
			        
			        Bitmap bmp = BitmapFactory.decodeResource(getResources(), resID);
					int width = bmp.getWidth();
					int height = (int)(bmp.getHeight() * .8);

					Bitmap cropped = Bitmap.createBitmap(bmp, 0, 0, width, height);
			        pokeSprites[me].setImageBitmap(cropped);
			        
			        //updateHPColors(me, hpBars[me].getProgress(), true);
			        netServ.battle.pokeChanged = false;
				}
			}
			if(netServ.battle.oppPokeChanged) {
				ShallowBattlePoke poke = netServ.battle.currentPoke(opp);
				// Load correct moveset and name
				if(poke != null) {
					currentPokeNames[opp].setText(netServ.battle.currentPoke(opp).rnick());
					hpBars[opp].setProgress(netServ.battle.currentPoke(opp).lifePercent);
					//updateHPColors(opp, hpBars[opp].getProgress(), true);
					int resID = getResources().getIdentifier("p" + poke.uID.pokeNum + "_front",
			        		"drawable", "com.pokebros.android.pokemononline");
					
					Bitmap bmp = BitmapFactory.decodeResource(getResources(), resID);
					int width = bmp.getWidth();
					int height = bmp.getHeight();
					int startHeight = (int)(height * .1);

					Bitmap cropped = Bitmap.createBitmap(bmp, 0, startHeight, width, height - startHeight);
			        //pokeSprites[me].setImageBitmap(cropped);
			        
					Drawable pokeSprite = getResources().getDrawable(resID);
			        pokeSprites[opp].setImageBitmap(cropped);
					netServ.battle.oppPokeChanged = false;
				}
			}
			
			for(int i = 0; i < 6; i++) {
				BattlePoke poke = netServ.battle.myTeam.pokes[i];
				pokeListNames[i].setText(poke.nick);
	    		pokeListHPs[i].setText(poke.currentHP +
	    				"/" + poke.totalHP);
			}
	    	netServ.battle.hist.append(delta);
			delta.clear();
			handler.postDelayed(animateHPBars, 50);
			handler.postDelayed(this, 1000);
		}
	};

	private ServiceConnection connection = new ServiceConnection() {
		public void onServiceConnected(ComponentName className, IBinder service) {
			netServ =	((NetworkService.LocalBinder)service).getService();
			netServ.herp();
			netServ.showNotification(BattleActivity.class, "Battle");
			Toast.makeText(BattleActivity.this, "Service connected",
                    Toast.LENGTH_SHORT).show();
			
			// Set the UI to display the correct info
	        me = netServ.battle.me;
	        opp = netServ.battle.opp;
	        // We don't know which timer is which until the battle starts,
	        // so set them here.
	        timers[me] = (TextView)findViewById(R.id.timerB);
	        timers[opp] = (TextView)findViewById(R.id.timerA);
	        names[me] = (TextView)findViewById(R.id.nameB);
	        names[opp] = (TextView)findViewById(R.id.nameA);
	        
	        names[me].setText(netServ.battle.players[me].nick);
	        names[opp].setText(netServ.battle.players[opp].nick);
	        
	        hpBars[me] = (TextProgressBar)findViewById(R.id.hpBarB);
	        hpBars[opp] = (TextProgressBar)findViewById(R.id.hpBarA);
	        
	        currentPokeNames[me] = (TextView)findViewById(R.id.currentPokeNameB);
	        currentPokeNames[opp] = (TextView)findViewById(R.id.currentPokeNameA);
	        
	        pokeSprites[me] = (ImageView)findViewById(R.id.pokeSpriteB);
	        pokeSprites[opp] = (ImageView)findViewById(R.id.pokeSpriteA);
	        
	        for(int i = 0; i < 2; i++)
	        	lastHPs[i] = netServ.battle.currentPoke(i).lifePercent;
	        
	        // Load scrollback
	        infoView.setText(netServ.battle.hist);
	    	infoScroll.post(new Runnable() {
	    		public void run() {
	    			infoScroll.smoothScrollTo(0, infoView.getMeasuredHeight());
	    		}
	    	});
	    	
	    	// Prompt a UI update of the pokemon
	        netServ.battle.pokeChanged = netServ.battle.oppPokeChanged = true;
	    	// Set up the UI polling and timer updating
	        handler.postDelayed(updateUITask, 50);
	        handler.postDelayed(updateTimeTask, 100);
		}
		
		public void onServiceDisconnected(ComponentName className) {
			netServ = null;
			Toast.makeText(BattleActivity.this, "Service disconnected",
					Toast.LENGTH_SHORT).show();
		}
	};
	
	@Override
	public void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		Toast.makeText(BattleActivity.this, "New Intent", Toast.LENGTH_SHORT);
	}
    
    @Override
    public void onDestroy() {
    	unbindService(connection);
    	super.onDestroy();
    }

    public void changeName(Message msg) {
    	if (msg != null && msg.obj != null) {
    		TextView myView = (TextView)findViewById(R.id.nameA);
    		myView.setText(msg.obj.toString());
    	}
    }
    
    public OnClickListener battleListener = new OnClickListener() {
    	public void onClick(View v) {
    		int id = v.getId();
    		// Check for attacks
    		for(int i = 0; i < 4; i++)
    			if(id == attack[i].getId())
    				netServ.socket.sendMessage(netServ.battle.constructAttack((byte)i), Command.BattleMessage);
    		for(int i = 0; i < 6; i++) {
    			if(id == pokeListButtons[i].getId()) {
    				netServ.socket.sendMessage(netServ.battle.constructSwitch((byte)i), Command.BattleMessage);
    				realViewSwitcher.snapToScreen(0);
    			}
    		}
    	}
    };
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.battleoptions, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	switch (item.getItemId())
        {
    case R.id.forfeit_yes:
    	//TODO: implement forfeit
    	break;
    case R.id.forfeit_no:
    	break;
    case R.id.draw:
    	break;
        }
        return true;
    }
}