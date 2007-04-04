package com.bretth.osm.transformer.xml.impl;

import java.util.Date;

import org.xml.sax.Attributes;

import com.bretth.osm.transformer.data.Node;
import com.bretth.osm.transformer.data.Tag;


public class NodeElementProcessor extends BaseElementProcessor implements TagListener {
	private static final String ELEMENT_NAME_TAG = "tag";
	private static final String ATTRIBUTE_NAME_ID = "id";
	private static final String ATTRIBUTE_NAME_TIMESTAMP = "timestamp";
	private static final String ATTRIBUTE_NAME_LATITUDE = "lat";
	private static final String ATTRIBUTE_NAME_LONGITUDE = "lon";
	
	private TagElementProcessor tagElementProcessor;
	private Node node;
	
	
	public NodeElementProcessor(BaseElementProcessor parentProcessor) {
		super(parentProcessor);
		
		tagElementProcessor = new TagElementProcessor(this, this);
	}
	
	
	public void reset() {
		node = null;
	}
	
	
	public void begin(Attributes attributes) {
		long id;
		Date timestamp;
		double latitude;
		double longitude;
		
		id = Long.parseLong(attributes.getValue(ATTRIBUTE_NAME_ID));
		timestamp = parseTimestamp(attributes.getValue(ATTRIBUTE_NAME_TIMESTAMP));
		latitude = Double.parseDouble(attributes.getValue(ATTRIBUTE_NAME_LATITUDE));
		longitude = Double.parseDouble(attributes.getValue(ATTRIBUTE_NAME_LONGITUDE));
		
		node = new Node(id, timestamp, latitude, longitude);
	}
	
	
	public ElementProcessor getChild(String uri, String localName, String qName) {
		if (ELEMENT_NAME_TAG.equals(qName)) {
			return tagElementProcessor;
		} else {
			return getDummyChildProcessor();
		}
	}
	
	
	public void end() {
		getOsmSink().addNode(node);
		reset();
	}
	
	
	public void processTag(Tag tag) {
		node.addTag(tag);
	}
}
