package com.pokebros.android.pokemononline;

import java.util.Comparator;
import java.util.Hashtable;

import com.pokebros.android.pokemononline.ServerListAdapter.Server;
import com.pokebros.android.pokemononline.player.BasicPlayerInfo;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class PlayerListAdapter extends ArrayAdapter<com.pokebros.android.pokemononline.player.BasicPlayerInfo>{
	
	public PlayerListAdapter(Context context, int resource) {
		super(context, resource);
	}
	
	public void addPlayer(BasicPlayerInfo p) {
		add(p);
	}
	
	public void removePlayer(BasicPlayerInfo p){
		remove(p);
	}
	
	public void sortByNick() {
		setNotifyOnChange(false);
		super.sort(new Comparator<BasicPlayerInfo>() {
			public int compare(BasicPlayerInfo pi1, BasicPlayerInfo pi2) {
				return pi1.nick.toLowerCase().compareTo(pi2.nick.toLowerCase());
			}
		});
		setNotifyOnChange(true);
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = convertView;
		if (view == null) {
			LayoutInflater inflater = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			view = inflater.inflate(R.layout.row_simple, null);
		}
		BasicPlayerInfo player = getItem(position);
		if (player != null) {
			TextView nick = (TextView)view.findViewById(R.id.player_list_name);
			nick.setText(player.nick);
		}
		return view;
	}
	
	@Override
	public void notifyDataSetChanged(){
		sortByNick();
		super.notifyDataSetChanged();
	}
}
