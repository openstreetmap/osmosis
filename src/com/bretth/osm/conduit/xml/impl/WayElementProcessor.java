package com.bretth.osm.conduit.xml.impl;

import java.util.Date;

import org.xml.sax.Attributes;

import com.bretth.osm.conduit.data.SegmentReference;
import com.bretth.osm.conduit.data.Tag;
import com.bretth.osm.conduit.data.Way;


/**
 * Provides an element processor implementation for a way.
 * 
 * @author Brett Henderson
 */
public class WayElementProcessor extends BaseElementProcessor implements TagListener, SegmentReferenceListener {
	private static final String ELEMENT_NAME_TAG = "tag";
	private static final String ELEMENT_NAME_SEGMENT = "seg";
	private static final String ATTRIBUTE_NAME_ID = "id";
	private static final String ATTRIBUTE_NAME_TIMESTAMP = "timestamp";
	
	private TagElementProcessor tagElementProcessor;
	private SegmentReferenceElementProcessor segmentReferenceElementProcessor;
	private Way way;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param parentProcessor
	 *            The parent element processor.
	 */
	public WayElementProcessor(BaseElementProcessor parentProcessor) {
		super(parentProcessor);
		
		tagElementProcessor = new TagElementProcessor(this, this);
		segmentReferenceElementProcessor = new SegmentReferenceElementProcessor(this, this);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void begin(Attributes attributes) {
		long id;
		Date timestamp;
		
		id = Long.parseLong(attributes.getValue(ATTRIBUTE_NAME_ID));
		timestamp = parseTimestamp(attributes.getValue(ATTRIBUTE_NAME_TIMESTAMP));
		
		way = new Way(id, timestamp);
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
		if (ELEMENT_NAME_SEGMENT.equals(qName)) {
			return segmentReferenceElementProcessor;
		} else if (ELEMENT_NAME_TAG.equals(qName)) {
			return tagElementProcessor;
		}
		
		return super.getChild(uri, localName, qName);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void end() {
		getOsmSink().addWay(way);
		way = null;
	}
	
	
	/**
	 * This is called by child element processors when a tag object is
	 * encountered.
	 * 
	 * @param tag
	 *            The tag to be processed.
	 */
	public void processTag(Tag tag) {
		way.addTag(tag);
	}


	/**
	 * This is called by child element processors when a segment reference
	 * object is encountered.
	 * 
	 * @param segmentReference
	 *            The segmentReference to be processed.
	 */
	public void processSegmentReference(SegmentReference segmentReference) {
		way.addSegmentReference(segmentReference);	
	}
}
