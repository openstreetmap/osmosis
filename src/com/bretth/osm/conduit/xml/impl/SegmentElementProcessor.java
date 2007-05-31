package com.bretth.osm.conduit.xml.impl;

import org.xml.sax.Attributes;

import com.bretth.osm.conduit.data.Segment;
import com.bretth.osm.conduit.data.Tag;
import com.bretth.osm.conduit.task.Sink;


/**
 * Provides an element processor implementation for a segment.
 * 
 * @author Brett Henderson
 */
public class SegmentElementProcessor extends SourceElementProcessor implements TagListener {
	private static final String ELEMENT_NAME_TAG = "tag";
	private static final String ATTRIBUTE_NAME_ID = "id";
	private static final String ATTRIBUTE_NAME_FROM = "from";
	private static final String ATTRIBUTE_NAME_TO = "to";
	
	private TagElementProcessor tagElementProcessor;
	private Segment segment;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param parentProcessor
	 *            The parent of this element processor.
	 * @param sink
	 *            The sink for receiving processed data.
	 */
	public SegmentElementProcessor(BaseElementProcessor parentProcessor, Sink sink) {
		super(parentProcessor, sink);
		
		tagElementProcessor = new TagElementProcessor(this, this);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void begin(Attributes attributes) {
		long id;
		long from;
		long to;
		
		id = Long.parseLong(attributes.getValue(ATTRIBUTE_NAME_ID));
		from = Long.parseLong(attributes.getValue(ATTRIBUTE_NAME_FROM));
		to = Long.parseLong(attributes.getValue(ATTRIBUTE_NAME_TO));
		
		segment = new Segment(id, from, to);
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
		if (ELEMENT_NAME_TAG.equals(qName)) {
			return tagElementProcessor;
		}
		
		return super.getChild(uri, localName, qName);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void end() {
		getSink().addSegment(segment);
		segment = null;
	}
	
	
	/**
	 * This is called by child element processors when a tag object is
	 * encountered.
	 * 
	 * @param tag
	 *            The tag to be processed.
	 */
	public void processTag(Tag tag) {
		segment.addTag(tag);
	}
}
