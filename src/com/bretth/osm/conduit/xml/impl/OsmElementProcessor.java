package com.bretth.osm.conduit.xml.impl;

import org.xml.sax.Attributes;

import com.bretth.osm.conduit.task.OsmSink;


/**
 * Provides an element processor implementation for an osm root element.
 * 
 * @author Brett Henderson
 */
public class OsmElementProcessor extends BaseElementProcessor {
	private static final String ELEMENT_NAME_NODE = "node";
	private static final String ELEMENT_NAME_SEGMENT = "segment";
	private static final String ELEMENT_NAME_WAY = "way";
	private static final String ATTRIBUTE_NAME_VERSION = "version";
	private static final String ATTRIBUTE_VALUE_VERSION = "0.3";
	
	
	private OsmSink osmSink;
	private NodeElementProcessor nodeElementProcessor;
	private SegmentElementProcessor segmentElementProcessor;
	private WayElementProcessor wayElementProcessor;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param osmSink
	 *            The destination for all processed data.
	 */
	public OsmElementProcessor(OsmSink osmSink) {
		super(null);
		
		this.osmSink = osmSink;
		
		nodeElementProcessor = new NodeElementProcessor(this);
		segmentElementProcessor = new SegmentElementProcessor(this);
		wayElementProcessor = new WayElementProcessor(this);
	}
	
	
	/**
	 * Returns the destination for all processed data.
	 * 
	 * @return The osm sink.
	 */
	@Override
	protected OsmSink getOsmSink() {
		return osmSink;
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
