package com.bretth.osm.transformer.xml.impl;

import org.xml.sax.Attributes;


public interface ElementProcessor {
	void reset();
	
	void begin(Attributes attributes);
	
	ElementProcessor getChild(String uri, String localName, String qName);
	
	ElementProcessor getParent();
	
	void end();
}
