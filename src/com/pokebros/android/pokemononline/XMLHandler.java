package com.pokebros.android.pokemononline;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class XMLHandler extends DefaultHandler {
	private boolean inTrainer = false;
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
	}

	/**
	 * Gets be called on the following structure: <tag>characters</tag>
	 */
	@Override
	public void characters(char ch[], int start, int length) {
		if (inTrainer) {
			myParsedTeam.setNick(new String(ch, start, length));
			inTrainer = false;
		}
	}
}