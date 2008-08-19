// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package com.bretth.osmosis.core.xml.v0_6.impl;

import org.xml.sax.Attributes;

import com.bretth.osmosis.core.container.v0_6.NodeContainer;
import com.bretth.osmosis.core.domain.common.TimestampContainer;
import com.bretth.osmosis.core.domain.v0_6.Node;
import com.bretth.osmosis.core.domain.v0_6.OsmUser;
import com.bretth.osmosis.core.domain.v0_6.Tag;
import com.bretth.osmosis.core.task.v0_6.Sink;
import com.bretth.osmosis.core.xml.common.BaseElementProcessor;
import com.bretth.osmosis.core.xml.common.ElementProcessor;


/**
 * Provides an element processor implementation for a node.
 * 
 * @author Brett Henderson
 */
public class NodeElementProcessor extends EntityElementProcessor implements TagListener {
	private static final String ELEMENT_NAME_TAG = "tag";
	private static final String ATTRIBUTE_NAME_ID = "id";
	private static final String ATTRIBUTE_NAME_TIMESTAMP = "timestamp";
	private static final String ATTRIBUTE_NAME_USER = "user";
	private static final String ATTRIBUTE_NAME_USERID = "uid";
	private static final String ATTRIBUTE_NAME_VERSION = "version";
	private static final String ATTRIBUTE_NAME_LATITUDE = "lat";
	private static final String ATTRIBUTE_NAME_LONGITUDE = "lon";
	
	private TagElementProcessor tagElementProcessor;
	private Node node;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param parentProcessor
	 *            The parent of this element processor.
	 * @param sink
	 *            The sink for receiving processed data.
	 * @param enableDateParsing
	 *            If true, dates will be parsed from xml data, else the current
	 *            date will be used thus saving parsing time.
	 */
	public NodeElementProcessor(BaseElementProcessor parentProcessor, Sink sink, boolean enableDateParsing) {
		super(parentProcessor, sink, enableDateParsing);
		
		tagElementProcessor = new TagElementProcessor(this, this);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void begin(Attributes attributes) {
		long id;
		int version;
		TimestampContainer timestampContainer;
		String rawUserId;
		String rawUserName;
		OsmUser user;
		double latitude;
		double longitude;
		
		id = Long.parseLong(attributes.getValue(ATTRIBUTE_NAME_ID));
		version = Integer.parseInt(attributes.getValue(ATTRIBUTE_NAME_VERSION));
		timestampContainer = createTimestampContainer(attributes.getValue(ATTRIBUTE_NAME_TIMESTAMP));
		rawUserId = attributes.getValue(ATTRIBUTE_NAME_USERID);
		rawUserName = attributes.getValue(ATTRIBUTE_NAME_USER);
		latitude = Double.parseDouble(attributes.getValue(ATTRIBUTE_NAME_LATITUDE));
		longitude = Double.parseDouble(attributes.getValue(ATTRIBUTE_NAME_LONGITUDE));
		
		user = buildUser(rawUserId, rawUserName);
		
		node = new Node(id, version, timestampContainer, user, latitude, longitude);
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
		getSink().process(new NodeContainer(node));
		node = null;
	}
	
	
	/**
	 * This is called by child element processors when a tag object is
	 * encountered.
	 * 
	 * @param tag
	 *            The tag to be processed.
	 */
	public void processTag(Tag tag) {
		node.addTag(tag);
	}
}
