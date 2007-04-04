package com.bretth.osm.transformer.xml.impl;

import org.xml.sax.Attributes;

import com.bretth.osm.transformer.data.Segment;
import com.bretth.osm.transformer.data.Tag;


public class SegmentElementProcessor extends BaseElementProcessor implements TagListener {
	private static final String ELEMENT_NAME_TAG = "tag";
	private static final String ATTRIBUTE_NAME_ID = "id";
	private static final String ATTRIBUTE_NAME_FROM = "from";
	private static final String ATTRIBUTE_NAME_TO = "to";
	
	private TagElementProcessor tagElementProcessor;
	private Segment segment;
	
	
	public SegmentElementProcessor(BaseElementProcessor parentProcessor) {
		super(parentProcessor);
		
		tagElementProcessor = new TagElementProcessor(this, this);
	}
	
	
	public void reset() {
		segment = null;
	}
	
	
	public void begin(Attributes attributes) {
		long id;
		long from;
		long to;
		
		id = Long.parseLong(attributes.getValue(ATTRIBUTE_NAME_ID));
		from = Long.parseLong(attributes.getValue(ATTRIBUTE_NAME_FROM));
		to = Long.parseLong(attributes.getValue(ATTRIBUTE_NAME_TO));
		
		segment = new Segment(id, from, to);
	}
	
	
	public ElementProcessor getChild(String uri, String localName, String qName) {
		if (ELEMENT_NAME_TAG.equals(qName)) {
			return tagElementProcessor;
		} else {
			return getDummyChildProcessor();
		}
	}
	
	
	public void end() {
		getOsmSink().addSegment(segment);
		reset();
	}
	
	
	public void processTag(Tag tag) {
		segment.addTag(tag);
	}
}
