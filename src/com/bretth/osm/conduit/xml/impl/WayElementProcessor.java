package com.bretth.osm.conduit.xml.impl;

import java.util.Date;

import org.xml.sax.Attributes;

import com.bretth.osm.conduit.data.SegmentReference;
import com.bretth.osm.conduit.data.Tag;
import com.bretth.osm.conduit.data.Way;


public class WayElementProcessor extends BaseElementProcessor implements TagListener, SegmentReferenceListener {
	private static final String ELEMENT_NAME_TAG = "tag";
	private static final String ELEMENT_NAME_SEGMENT = "seg";
	private static final String ATTRIBUTE_NAME_ID = "id";
	private static final String ATTRIBUTE_NAME_TIMESTAMP = "timestamp";
	
	private TagElementProcessor tagElementProcessor;
	private SegmentReferenceElementProcessor segmentReferenceElementProcessor;
	private Way way;
	
	
	public WayElementProcessor(BaseElementProcessor parentProcessor) {
		super(parentProcessor);
		
		tagElementProcessor = new TagElementProcessor(this, this);
		segmentReferenceElementProcessor = new SegmentReferenceElementProcessor(this, this);
	}
	
	
	public void reset() {
		way = null;
	}
	
	
	public void begin(Attributes attributes) {
		long id;
		Date timestamp;
		
		id = Long.parseLong(attributes.getValue(ATTRIBUTE_NAME_ID));
		timestamp = parseTimestamp(attributes.getValue(ATTRIBUTE_NAME_TIMESTAMP));
		
		way = new Way(id, timestamp);
	}
	
	
	public ElementProcessor getChild(String uri, String localName, String qName) {
		if (ELEMENT_NAME_SEGMENT.equals(qName)) {
			return segmentReferenceElementProcessor;
		} else if (ELEMENT_NAME_TAG.equals(qName)) {
				return tagElementProcessor;
		} else {
			return getDummyChildProcessor();
		}
	}
	
	
	public void end() {
		getOsmSink().addWay(way);
		reset();
	}
	
	
	public void processTag(Tag tag) {
		way.addTag(tag);
	}


	public void processSegmentReference(SegmentReference segmentReference) {
		way.addSegmentReference(segmentReference);	
	}
}
