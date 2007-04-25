package com.bretth.osm.conduit.xml.impl;

import org.xml.sax.Attributes;

import com.bretth.osm.conduit.task.OsmSink;


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
	
	
	public OsmElementProcessor(OsmSink osmSink) {
		super(null);
		
		this.osmSink = osmSink;
		
		nodeElementProcessor = new NodeElementProcessor(this);
		segmentElementProcessor = new SegmentElementProcessor(this);
		wayElementProcessor = new WayElementProcessor(this);
	}
	
	
	@Override
	protected OsmSink getOsmSink() {
		return osmSink;
	}
	
	
	public void reset() {
		// This class maintains no state and doesn't require a reset.
	}
	
	
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
	
	
	public ElementProcessor getChild(String uri, String localName, String qName) {
		if (ELEMENT_NAME_NODE.equals(qName)) {
			return nodeElementProcessor;
		} else if (ELEMENT_NAME_SEGMENT.equals(qName)) {
			return segmentElementProcessor;
		} else if (ELEMENT_NAME_WAY.equals(qName)) {
			return wayElementProcessor;
		} else {
			return getDummyChildProcessor();
		}
	}
	
	
	public void end() {
		// This class produces no data and therefore doesn't need to do anything
		// when the end of the element is reached.
	}
}
