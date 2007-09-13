package com.bretth.osmosis.core.xml.v0_5.impl;

import org.xml.sax.Attributes;

import com.bretth.osmosis.core.domain.v0_5.Tag;
import com.bretth.osmosis.core.xml.common.BaseElementProcessor;


/**
 * Provides an element processor implementation for a tag.
 * 
 * @author Brett Henderson
 */
public class TagElementProcessor extends BaseElementProcessor {
	private static final String ATTRIBUTE_NAME_KEY = "k";
	private static final String ATTRIBUTE_NAME_VALUE = "v";
	
	private TagListener tagListener;
	private Tag tag;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param parentProcessor
	 *            The parent element processor.
	 * @param tagListener
	 *            The tag listener for receiving created tags.
	 */
	public TagElementProcessor(BaseElementProcessor parentProcessor, TagListener tagListener) {
		super(parentProcessor, true);
		
		this.tagListener = tagListener;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void begin(Attributes attributes) {
		String key;
		String value;
		
		key = attributes.getValue(ATTRIBUTE_NAME_KEY);
		value = attributes.getValue(ATTRIBUTE_NAME_VALUE);
		
		tag = new Tag(key, value);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void end() {
		tagListener.processTag(tag);
		tag = null;
	}
}
