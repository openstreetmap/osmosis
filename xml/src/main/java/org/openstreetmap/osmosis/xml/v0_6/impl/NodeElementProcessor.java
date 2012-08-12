// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.xml.v0_6.impl;

import org.xml.sax.Attributes;

import org.openstreetmap.osmosis.core.container.v0_6.NodeContainer;
import org.openstreetmap.osmosis.core.domain.common.TimestampContainer;
import org.openstreetmap.osmosis.core.domain.v0_6.CommonEntityData;
import org.openstreetmap.osmosis.core.domain.v0_6.Node;
import org.openstreetmap.osmosis.core.domain.v0_6.OsmUser;
import org.openstreetmap.osmosis.core.domain.v0_6.Tag;
import org.openstreetmap.osmosis.core.task.v0_6.Sink;
import org.openstreetmap.osmosis.xml.common.BaseElementProcessor;
import org.openstreetmap.osmosis.xml.common.ElementProcessor;
import org.openstreetmap.osmosis.core.OsmosisRuntimeException;


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
	private static final String ATTRIBUTE_NAME_CHANGESET_ID = "changeset";
	private static final String ATTRIBUTE_NAME_VERSION = "version";
	private static final String ATTRIBUTE_NAME_LATITUDE = "lat";
	private static final String ATTRIBUTE_NAME_LONGITUDE = "lon";
	
	private TagElementProcessor tagElementProcessor;
	private Node node;
	private boolean coordinatesRequired;
	
	

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
		this(parentProcessor, sink, enableDateParsing, true);
	}
	
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
	 * @param coordinatesRequired
	 * 		      If true, nodes without lat and lon attributes set will cause an exception.
	 */
	public NodeElementProcessor(BaseElementProcessor parentProcessor, Sink sink, boolean enableDateParsing, 
			boolean coordinatesRequired) {
		super(parentProcessor, sink, enableDateParsing);

		this.coordinatesRequired = coordinatesRequired;
		tagElementProcessor = new TagElementProcessor(this, this);
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
		double latitude;
		double longitude;
		
		id = Long.parseLong(attributes.getValue(ATTRIBUTE_NAME_ID));
		sversion = attributes.getValue(ATTRIBUTE_NAME_VERSION);
		if (sversion == null) {
			throw new OsmosisRuntimeException("Node " + id
					+ " does not have a version attribute as OSM 0.6 are required to have.  Is this a 0.5 file?");
		} else {
			version = Integer.parseInt(sversion);
		}
		timestampContainer = createTimestampContainer(attributes.getValue(ATTRIBUTE_NAME_TIMESTAMP));
		rawUserId = attributes.getValue(ATTRIBUTE_NAME_USERID);
		rawUserName = attributes.getValue(ATTRIBUTE_NAME_USER);
		changesetId = buildChangesetId(attributes.getValue(ATTRIBUTE_NAME_CHANGESET_ID));
		
		latitude = getLatLonDouble(attributes, ATTRIBUTE_NAME_LATITUDE, id);
		longitude = getLatLonDouble(attributes, ATTRIBUTE_NAME_LONGITUDE, id);
		
		user = buildUser(rawUserId, rawUserName);
		
		node = new Node(new CommonEntityData(id, version, timestampContainer, user, changesetId), latitude, longitude);
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
	}
	
	
	/**
	 * This is called by child element processors when a tag object is
	 * encountered.
	 * 
	 * @param tag
	 *            The tag to be processed.
	 */
	public void processTag(Tag tag) {
		node.getTags().add(tag);
	}
	
	private double getLatLonDouble(Attributes attributes, String attributeName, long id) {
		String value = attributes.getValue(attributeName);
		if (value == null) {
			if (coordinatesRequired) {
				throw new OsmosisRuntimeException(String.format(
						"Node %s does not have its %s attribute set; this attribute is required in current context.",
						id, attributeName));
			} else {
				return Double.NaN;
			}
		}
		
		try {
			return Double.parseDouble(value);
		} catch (NumberFormatException ex) {
			throw new OsmosisRuntimeException(String.format(
					"Node %s: cannot parse the %s attribute as a numeric value",
					id, attributeName));
		}
	}
}
