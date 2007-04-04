package com.bretth.osm.transformer.xml.impl;

import org.xml.sax.Attributes;


public class DummyElementProcessor extends BaseElementProcessor {
	
	private int nestedElementCount;
	
	
	public DummyElementProcessor(BaseElementProcessor parentProcessor) {
		super(parentProcessor);
	}
	
	
	public void reset() {
		nestedElementCount = 0;
	}
	
	
	public void begin(Attributes attributes) {
	}
	
	
	public ElementProcessor getChild(String uri, String localName, String qName) {
		nestedElementCount++;
		
		return this;
	}
	
	
	public ElementProcessor getParent() {
		if (nestedElementCount > 0) {
			nestedElementCount--;
			return this;
		} else {
			return super.getParent();
		}
	}
	
	
	public void end() {
	}
}
