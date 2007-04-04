package com.bretth.osm.conduit.xml.impl;

import org.xml.sax.Attributes;

import com.bretth.osm.conduit.data.SegmentReference;


public class SegmentReferenceElementProcessor extends BaseElementProcessor {
	private static final String ATTRIBUTE_NAME_ID = "id";
	
	private SegmentReferenceListener segmentReferenceListener;
	private SegmentReference segmentReference;
	
	
	public SegmentReferenceElementProcessor(BaseElementProcessor parentProcessor, SegmentReferenceListener segmentReferenceListener) {
		super(parentProcessor);
		
		this.segmentReferenceListener = segmentReferenceListener;
	}
	
	
	public void reset() {
		segmentReference = null;
	}
	
	
	public void begin(Attributes attributes) {
		long id;
		
		id = Long.parseLong(attributes.getValue(ATTRIBUTE_NAME_ID));
		
		segmentReference = new SegmentReference(id);
	}
	
	
	public ElementProcessor getChild(String uri, String localName, String qName) {
		return getDummyChildProcessor();
	}
	
	
	public void end() {
		segmentReferenceListener.processSegmentReference(segmentReference);
	}
}
