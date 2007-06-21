package com.bretth.osmosis.xml.impl;

import org.xml.sax.Attributes;

import com.bretth.osmosis.data.SegmentReference;


/**
 * Provides an element processor implementation for a segment reference.
 * 
 * @author Brett Henderson
 */
public class SegmentReferenceElementProcessor extends BaseElementProcessor {
	private static final String ATTRIBUTE_NAME_ID = "id";
	
	private SegmentReferenceListener segmentReferenceListener;
	private SegmentReference segmentReference;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param parentProcessor
	 *            The parent element processor.
	 * @param segmentReferenceListener
	 *            The segment reference listener for receiving created tags.
	 */
	public SegmentReferenceElementProcessor(BaseElementProcessor parentProcessor, SegmentReferenceListener segmentReferenceListener) {
		super(parentProcessor);
		
		this.segmentReferenceListener = segmentReferenceListener;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void begin(Attributes attributes) {
		long id;
		
		id = Long.parseLong(attributes.getValue(ATTRIBUTE_NAME_ID));
		
		segmentReference = new SegmentReference(id);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void end() {
		segmentReferenceListener.processSegmentReference(segmentReference);
		segmentReference = null;
	}
}
