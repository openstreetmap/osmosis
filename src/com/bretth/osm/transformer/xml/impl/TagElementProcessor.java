package com.bretth.osm.transformer.xml.impl;

import org.xml.sax.Attributes;

import com.bretth.osm.transformer.data.Tag;


public class TagElementProcessor extends BaseElementProcessor {
	private static final String ATTRIBUTE_NAME_KEY = "k";
	private static final String ATTRIBUTE_NAME_VALUE = "v";
	
	private TagListener tagListener;
	private Tag tag;
	
	
	public TagElementProcessor(BaseElementProcessor parentProcessor, TagListener tagListener) {
		super(parentProcessor);
		
		this.tagListener = tagListener;
	}
	
	
	public void reset() {
		tag = null;
	}
	
	
	public void begin(Attributes attributes) {
		String key;
		String value;
		
		key = attributes.getValue(ATTRIBUTE_NAME_KEY);
		value = attributes.getValue(ATTRIBUTE_NAME_VALUE);
		
		tag = new Tag(key, value);
	}
	
	
	public ElementProcessor getChild(String uri, String localName, String qName) {
		return getDummyChildProcessor();
	}
	
	
	public void end() {
		tagListener.processTag(tag);
	}
}
