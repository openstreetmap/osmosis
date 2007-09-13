package com.bretth.osmosis.core.xml.v0_5.impl;

import org.xml.sax.Attributes;

import com.bretth.osmosis.core.domain.v0_5.WayNode;
import com.bretth.osmosis.core.xml.common.BaseElementProcessor;


/**
 * Provides an element processor implementation for a segment reference.
 * 
 * @author Brett Henderson
 */
public class WayNodeElementProcessor extends BaseElementProcessor {
	private static final String ATTRIBUTE_NAME_ID = "id";
	
	private WayNodeListener wayNodeListener;
	private WayNode wayNode;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param parentProcessor
	 *            The parent element processor.
	 * @param segmentReferenceListener
	 *            The segment reference listener for receiving created tags.
	 */
	public WayNodeElementProcessor(BaseElementProcessor parentProcessor, WayNodeListener segmentReferenceListener) {
		super(parentProcessor, true);
		
		this.wayNodeListener = segmentReferenceListener;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void begin(Attributes attributes) {
		long id;
		
		id = Long.parseLong(attributes.getValue(ATTRIBUTE_NAME_ID));
		
		wayNode = new WayNode(id);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void end() {
		wayNodeListener.processWayNode(wayNode);
		wayNode = null;
	}
}
