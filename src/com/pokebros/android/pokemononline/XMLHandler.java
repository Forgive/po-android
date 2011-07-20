package com.pokebros.android.pokemononline;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.pokebros.android.pokemononline.poke.TeamPoke;

public class XMLHandler extends DefaultHandler {
	private boolean inTrainer = false;
	private boolean inPokemon = false;
	private boolean inMove = false;
	private boolean inDV = false;
	private boolean inEV = false;
	private int numPoke = 0;
	private int numMove = 0;
	private int numEV = 0;
	private int numDV = 0;
	private TeamPoke[] myTeam = new TeamPoke[6];
	private XMLDataSet myParsedTeam = new XMLDataSet();

	public XMLDataSet getParsedData() {
		return this.myParsedTeam;
	}

	@Override
	public void startDocument() throws SAXException {
		this.myParsedTeam = new XMLDataSet();
	}

	@Override
	public void endDocument() throws SAXException {
		// Nothing to do
	}

	/**
	 * Gets be called on opening tags like: <tag> Can provide attribute(s), when
	 * xml was like: <tag attribute="attributeValue">
	 */
	@Override
	public void startElement(String namespaceURI, String localName,
			String qName, Attributes atts) throws SAXException {
		if (localName.equals("Team")) {
			String gen = atts.getValue("gen");
			byte g = (byte)(Integer.parseInt(gen));
			myParsedTeam.setGen(g);
			String DefaultTier = atts.getValue("defaultTier");
			myParsedTeam.setDefaultTier(DefaultTier);
		}
		else if (localName.equals("Trainer")) {
			inTrainer = true;
			String loseMsg = atts.getValue("loseMsg");
			myParsedTeam.setLoseMsg(loseMsg);
			String avatar = atts.getValue("avatar");
			short a = (short) (Integer.parseInt(avatar));
			myParsedTeam.setAvatar(a);
			String winMsg = atts.getValue("winMsg");
			myParsedTeam.setWinMsg(winMsg);
			String infoMsg = atts.getValue("infoMsg");
			myParsedTeam.setInfo(infoMsg);
		}
		else if (localName.equals("Pokemon")) {
			inPokemon = true;
			String pokeNum = atts.getValue("Num");
			short pn = (short)(Integer.parseInt(pokeNum));
			myParsedTeam.setPokeNum(pn);
			String subNum = atts.getValue("Forme");
			byte sn = (byte)(Integer.parseInt(subNum));
			myParsedTeam.setSubNum(sn);
		}
	}

	/**
	 * Gets be called on closing tags like: </tag>
	 */
	@Override
	public void endElement(String namespaceURI, String localName, String qName)
			throws SAXException {
		if (localName.equals("Trainer")) {
			inTrainer = false;
		}
		if (localName.equals("Pokemon")) {
			inPokemon = false;
		}
		if (localName.equals("Move")) {
			inPokemon = false;
		}
		if (localName.equals("DV")) {
			inPokemon = false;
		}
		if (localName.equals("EV")) {
			inPokemon = false;
		}
	}

	/**
	 * Gets be called on the following structure: <tag>characters</tag>
	 */
	@Override
	public void characters(char ch[], int start, int length) {
		if (inTrainer) {
			myParsedTeam.setNick(new String(ch, start, length));
		}
	}
}