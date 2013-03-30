// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.xml.v0_6.impl;

import org.openstreetmap.osmosis.core.container.v0_6.WayContainer;
import org.openstreetmap.osmosis.core.domain.common.TimestampContainer;
import org.openstreetmap.osmosis.core.domain.v0_6.CommonEntityData;
import org.openstreetmap.osmosis.core.domain.v0_6.OsmUser;
import org.openstreetmap.osmosis.core.domain.v0_6.Tag;
import org.openstreetmap.osmosis.core.domain.v0_6.Way;
import org.openstreetmap.osmosis.core.domain.v0_6.WayNode;
import org.openstreetmap.osmosis.core.task.v0_6.Sink;
import org.openstreetmap.osmosis.xml.common.BaseElementProcessor;
import org.openstreetmap.osmosis.xml.common.ElementProcessor;
import org.xml.sax.Attributes;
import org.openstreetmap.osmosis.core.OsmosisRuntimeException;


/**
 * Provides an element processor implementation for a way.
 * 
 * @author Brett Henderson
 */
public class WayElementProcessor extends EntityElementProcessor implements TagListener, WayNodeListener {
	private static final String ELEMENT_NAME_TAG = "tag";
	private static final String ELEMENT_NAME_NODE = "nd";
	private static final String ATTRIBUTE_NAME_ID = "id";
	private static final String ATTRIBUTE_NAME_TIMESTAMP = "timestamp";
	private static final String ATTRIBUTE_NAME_USER = "user";
	private static final String ATTRIBUTE_NAME_USERID = "uid";
	private static final String ATTRIBUTE_NAME_CHANGESET_ID = "changeset";
	private static final String ATTRIBUTE_NAME_VERSION = "version";
	
	private TagElementProcessor tagElementProcessor;
	private WayNodeElementProcessor wayNodeElementProcessor;
	private Way way;
	
	
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
	public WayElementProcessor(BaseElementProcessor parentProcessor, Sink sink, boolean enableDateParsing) {
		super(parentProcessor, sink, enableDateParsing);
		
		tagElementProcessor = new TagElementProcessor(this, this);
		wayNodeElementProcessor = new WayNodeElementProcessor(this, this);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void begin(Attributes attributes) {
		long id;
		String sversion;
		int version;
		TimestampContainer timestampContainer;
		String rawUserId;
		String rawUserName;
		OsmUser user;
		long changesetId;
		
		id = Long.parseLong(attributes.getValue(ATTRIBUTE_NAME_ID));
		sversion = attributes.getValue(ATTRIBUTE_NAME_VERSION);
		if (sversion == null) {
			throw new OsmosisRuntimeException("Way " + id
					+ " does not have a version attribute as OSM 0.6 are required to have.  Is this a 0.5 file?");
		} else {
			version = Integer.parseInt(sversion);
		}
		timestampContainer = createTimestampContainer(attributes.getValue(ATTRIBUTE_NAME_TIMESTAMP));
		rawUserId = attributes.getValue(ATTRIBUTE_NAME_USERID);
		rawUserName = attributes.getValue(ATTRIBUTE_NAME_USER);
		changesetId = buildChangesetId(attributes.getValue(ATTRIBUTE_NAME_CHANGESET_ID));
		
		user = buildUser(rawUserId, rawUserName);
		
		way = new Way(new CommonEntityData(id, version, timestampContainer, user, changesetId));
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
			return wayNodeElementProcessor;
		} else if (ELEMENT_NAME_TAG.equals(qName)) {
			return tagElementProcessor;
		}
		
		return super.getChild(uri, localName, qName);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void end() {
		getSink().process(new WayContainer(way));
	}
	
	
	/**
	 * This is called by child element processors when a tag object is
	 * encountered.
	 * 
	 * @param tag
	 *            The tag to be processed.
	 */
	public void processTag(Tag tag) {
		way.getTags().add(tag);
	}
	
	
	/**
	 * This is called by child element processors when a way node object is
	 * encountered.
	 * 
	 * @param wayNode
	 *            The wayNode to be processed.
	 */
	public void processWayNode(WayNode wayNode) {
		way.getWayNodes().add(wayNode);
	}
}
