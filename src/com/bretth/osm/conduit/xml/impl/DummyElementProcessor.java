package com.bretth.osm.conduit.xml.impl;

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
		// Nothing to do because we're not processing this element.
	}
	
	
	public ElementProcessor getChild(String uri, String localName, String qName) {
		nestedElementCount++;
		
		return this;
	}
	
	
	@Override
	public ElementProcessor getParent() {
		if (nestedElementCount > 0) {
			nestedElementCount--;
			return this;
		} else {
			return super.getParent();
		}
	}
	
	
	public void end() {
		// Nothing to do because we're not processing this element.
	}
}
