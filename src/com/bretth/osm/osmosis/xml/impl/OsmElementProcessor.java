package com.bretth.osm.osmosis.xml.impl;

import org.xml.sax.Attributes;

import com.bretth.osm.osmosis.task.Sink;


/**
 * Provides an element processor implementation for an osm element.
 * 
 * @author Brett Henderson
 */
public class OsmElementProcessor extends SourceElementProcessor {
	private static final String ELEMENT_NAME_NODE = "node";
	private static final String ELEMENT_NAME_SEGMENT = "segment";
	private static final String ELEMENT_NAME_WAY = "way";
	private static final String ATTRIBUTE_NAME_VERSION = "version";
	private static final String ATTRIBUTE_VALUE_VERSION = "0.3";
	
	
	private NodeElementProcessor nodeElementProcessor;
	private SegmentElementProcessor segmentElementProcessor;
	private WayElementProcessor wayElementProcessor;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param parentProcessor
	 *            The parent of this element processor.
	 * @param sink
	 *            The sink for receiving processed data.
	 */
	public OsmElementProcessor(BaseElementProcessor parentProcessor, Sink sink) {
		super(parentProcessor, sink);
		
		nodeElementProcessor = new NodeElementProcessor(this, getSink());
		segmentElementProcessor = new SegmentElementProcessor(this, getSink());
		wayElementProcessor = new WayElementProcessor(this, getSink());
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void begin(Attributes attributes) {
		String fileVersion;
		
		fileVersion = attributes.getValue(ATTRIBUTE_NAME_VERSION);
		
		if (!ATTRIBUTE_VALUE_VERSION.equals(fileVersion)) {
			System.err.println(
				"Warning, expected version " + ATTRIBUTE_VALUE_VERSION
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
		} else if (ELEMENT_NAME_SEGMENT.equals(qName)) {
			return segmentElementProcessor;
		} else if (ELEMENT_NAME_WAY.equals(qName)) {
			return wayElementProcessor;
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
