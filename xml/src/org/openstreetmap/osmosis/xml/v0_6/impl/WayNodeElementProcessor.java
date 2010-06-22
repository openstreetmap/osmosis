// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.xml.v0_6.impl;

import org.xml.sax.Attributes;

import org.openstreetmap.osmosis.core.domain.v0_6.WayNode;
import org.openstreetmap.osmosis.xml.common.BaseElementProcessor;


/**
 * Provides an element processor implementation for a way node.
 * 
 * @author Brett Henderson
 */
public class WayNodeElementProcessor extends BaseElementProcessor {
	private static final String ATTRIBUTE_NAME_ID = "ref";
	
	private WayNodeListener wayNodeListener;
	private WayNode wayNode;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param parentProcessor
	 *            The parent element processor.
	 * @param wayNodeListener
	 *            The way node listener for receiving created tags.
	 */
	public WayNodeElementProcessor(BaseElementProcessor parentProcessor, WayNodeListener wayNodeListener) {
		super(parentProcessor, true);
		
		this.wayNodeListener = wayNodeListener;
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
