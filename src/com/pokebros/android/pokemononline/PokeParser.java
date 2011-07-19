package com.pokebros.android.pokemononline;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import com.pokebros.android.pokemononline.player.PlayerTeam;
import com.pokebros.android.pokemononline.poke.TeamPoke;

public class PokeParser extends DefaultHandler
{
	FileInputStream in;
	InputStreamReader isr;
	BufferedReader inRd;
	TeamPoke tp;
	PlayerTeam pt;
	XMLDataSet parsedTeam;
	
	public PokeParser() throws Exception {
		in = new FileInputStream("/sdcard/team.xml");
		isr = new InputStreamReader(in);
		 
		inRd = new BufferedReader(isr);
		/* Get a SAXParser from the SAXPArserFactory. */
		SAXParserFactory spf = SAXParserFactory.newInstance();
		SAXParser sp = spf.newSAXParser();

		/* Get the XMLReader of the SAXParser we created. */
		XMLReader xr = sp.getXMLReader();
		/* Create a new ContentHandler and apply it to the XML-Reader */
		XMLHandler myHandler = new XMLHandler();
		xr.setContentHandler(myHandler);

		FileInputStream in = new FileInputStream("/sdcard/team.xml");

		xr.parse(new InputSource(in));
		parsedTeam = myHandler.getParsedData();
	}

	public String getNick() {
		return parsedTeam.getNick();
	}
	
	public String getInfo() {
		return parsedTeam.getInfo();
	}
	
	public String getLoseMsg() {
		return parsedTeam.getLoseMsg();
	}
	
	public String getWinMsg() {
		return parsedTeam.getWinMsg();
	}
	
	public short getAvatar() {
		return parsedTeam.getAvatar();
	}
	
	public String getDefaultTier() {
		return parsedTeam.getDefaultTier();
	}
}