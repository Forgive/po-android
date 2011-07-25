package com.pokebros.android.pokemononline;

import java.util.Comparator;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class ChannelListAdapter extends ArrayAdapter<com.pokebros.android.pokemononline.Channel>{
	
	public ChannelListAdapter(Context context, int resource) {
		super(context, resource);
	}
	
	public void addChannel(Channel ch) {
		add(ch);
	}
	
	public void removeChannel(Channel ch){
		remove(ch);
	}
	
	public void sortByName() {
		super.sort(new Comparator<Channel>() {
			public int compare(Channel ch1, Channel ch2) {
				return ch1.name().compareTo(ch2.name());
			}
		});
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = convertView;
		if (view == null) {
			LayoutInflater inflater = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			view = inflater.inflate(R.layout.row_simple, null);
		}
		Channel channel = getItem(position);
		if (channel != null) {
			TextView nick = (TextView)view.findViewById(R.id.player_list_name);
			nick.setText(channel.name());
		}
		return view;
		
	}
}



