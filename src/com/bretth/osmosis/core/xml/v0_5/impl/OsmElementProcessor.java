// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package com.bretth.osmosis.core.xml.v0_5.impl;

import java.util.logging.Logger;

import org.xml.sax.Attributes;

import com.bretth.osmosis.core.task.v0_5.Sink;
import com.bretth.osmosis.core.xml.common.BaseElementProcessor;
import com.bretth.osmosis.core.xml.common.ElementProcessor;


/**
 * Provides an element processor implementation for an osm element.
 * 
 * @author Brett Henderson
 */
public class OsmElementProcessor extends SourceElementProcessor {
	
	private static final Logger log = Logger.getLogger(OsmElementProcessor.class.getName());
	
	private static final String ELEMENT_NAME_NODE = "node";
	private static final String ELEMENT_NAME_WAY = "way";
	private static final String ELEMENT_NAME_RELATION = "relation";
	private static final String ATTRIBUTE_NAME_VERSION = "version";
	
	
	private NodeElementProcessor nodeElementProcessor;
	private WayElementProcessor wayElementProcessor;
	private RelationElementProcessor relationElementProcessor;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param parentProcessor
	 *            The parent of this element processor.
	 * @param sink
	 *            The sink for receiving processed data.
	 * @param enableDateParsing
	 *            If true, dates will be parsed from xml data, else the current
	 *            date will be used thus saving parsing time.
	 */
	public OsmElementProcessor(BaseElementProcessor parentProcessor, Sink sink, boolean enableDateParsing) {
		super(parentProcessor, sink, enableDateParsing);
		
		nodeElementProcessor = new NodeElementProcessor(this, getSink(), enableDateParsing);
		wayElementProcessor = new WayElementProcessor(this, getSink(), enableDateParsing);
		relationElementProcessor = new RelationElementProcessor(this, getSink(), enableDateParsing);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void begin(Attributes attributes) {
		String fileVersion;
		
		fileVersion = attributes.getValue(ATTRIBUTE_NAME_VERSION);
		
		if (!XmlConstants.OSM_VERSION.equals(fileVersion)) {
			log.warning(
				"Expected version " + XmlConstants.OSM_VERSION
				+ " but received " + fileVersion + "."
			);
		}
	}
	
	
	/**
	 * Retrieves the appropriate child element processor for the newly
	 * encountered nested element.
	 * 
	 * @param uri
	 *            The element uri.
	 * @param localName
	 *            The element localName.
	 * @param qName
	 *            The element qName.
	 * @return The appropriate element processor for the nested element.
	 */
	@Override
	public ElementProcessor getChild(String uri, String localName, String qName) {
		if (ELEMENT_NAME_NODE.equals(qName)) {
			return nodeElementProcessor;
		} else if (ELEMENT_NAME_WAY.equals(qName)) {
			return wayElementProcessor;
		} else if (ELEMENT_NAME_RELATION.equals(qName)) {
			return relationElementProcessor;
		}
		
		return super.getChild(uri, localName, qName);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void end() {
		// This class produces no data and therefore doesn't need to do anything
		// when the end of the element is reached.
	}
}
