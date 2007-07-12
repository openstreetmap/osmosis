package com.bretth.osmosis.xml.impl;

import java.util.Date;


/**
 * Provides common functionality shared by element processor implementations.
 * 
 * @author Brett Henderson
 */
public abstract class BaseElementProcessor implements ElementProcessor {
	private BaseElementProcessor parentProcessor;
	private ElementProcessor dummyChildProcessor;
	private DateParser dateParser;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param parentProcessor
	 *            The parent of this element processor.
	 */
	protected BaseElementProcessor(BaseElementProcessor parentProcessor) {
		this.parentProcessor = parentProcessor;
		
		dateParser = new DateParser();
	}
	
	
	/**
	 * This implementation returns a dummy element processor as the child which
	 * ignores all nested xml elements. Sub-classes wishing to handle child
	 * elements must override this method and delegate to this method for xml
	 * elements they don't care about.
	 * 
	 * @param uri
	 *            The element uri.
	 * @param localName
	 *            The element localName.
	 * @param qName
	 *            The element qName.
	 * @return A dummy element processor.
	 */
	public ElementProcessor getChild(String uri, String localName, String qName) {
		if (dummyChildProcessor == null) {
			dummyChildProcessor = new DummyElementProcessor(this);
		}
		
		return dummyChildProcessor;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public ElementProcessor getParent() {
		return parentProcessor;
	}
	
	
	/**
	 * Parses a date using the standard osm date format.
	 * 
	 * @param data
	 *            The date string to be parsed.
	 * @return The parsed date.
	 */
	protected Date parseTimestamp(String data) {
		if (data != null && data.length() > 0) {
			return dateParser.parse(data);
		} else {
			return null;
		}
	}
}
