package com.bretth.osm.conduit.xml.impl;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.bretth.osm.conduit.ConduitRuntimeException;
import com.bretth.osm.conduit.task.OsmSink;


public class OsmHandler extends DefaultHandler {
	private static final String ELEMENT_NAME_OSM = "osm";
	
	
	private ElementProcessor elementProcessor;
	private ElementProcessor osmElementProcessor;
	
	
	public OsmHandler(OsmSink osmSink) {
		osmElementProcessor = new OsmElementProcessor(osmSink);
	}
	
	
	/**
	 * Set internal state back to defaults.
	 */
	private void reset() {
		elementProcessor = null;
		osmElementProcessor.reset();
	}
	
	
	@Override
	public void startDocument() throws SAXException {
		reset();
	}
	
	
	@Override
	public void endDocument() throws SAXException {
	}
	
	
	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		if (elementProcessor != null) {
			elementProcessor = elementProcessor.getChild(uri, localName, qName);
		} else if (ELEMENT_NAME_OSM.equals(qName)) {
			elementProcessor = osmElementProcessor;
		} else {
			throw new ConduitRuntimeException("This does not appear to be an OSM XML file.");
		}
		
		elementProcessor.begin(attributes);
	}
	
	
	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		elementProcessor.end();
		elementProcessor = elementProcessor.getParent();
	}
	
	
	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		super.characters(ch, start, length);
	}
}
