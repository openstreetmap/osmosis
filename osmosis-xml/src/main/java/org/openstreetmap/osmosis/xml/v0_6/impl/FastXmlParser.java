// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.xml.v0_6.impl;

import java.util.Calendar;
import java.util.logging.Logger;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.openstreetmap.osmosis.core.OsmosisRuntimeException;
import org.openstreetmap.osmosis.core.container.v0_6.BoundContainer;
import org.openstreetmap.osmosis.core.container.v0_6.NodeContainer;
import org.openstreetmap.osmosis.core.container.v0_6.RelationContainer;
import org.openstreetmap.osmosis.core.container.v0_6.WayContainer;
import org.openstreetmap.osmosis.core.domain.common.SimpleTimestampContainer;
import org.openstreetmap.osmosis.core.domain.common.TimestampContainer;
import org.openstreetmap.osmosis.core.domain.common.TimestampFormat;
import org.openstreetmap.osmosis.core.domain.common.UnparsedTimestampContainer;
import org.openstreetmap.osmosis.core.domain.v0_6.Bound;
import org.openstreetmap.osmosis.core.domain.v0_6.CommonEntityData;
import org.openstreetmap.osmosis.core.domain.v0_6.EntityType;
import org.openstreetmap.osmosis.core.domain.v0_6.Node;
import org.openstreetmap.osmosis.core.domain.v0_6.OsmUser;
import org.openstreetmap.osmosis.core.domain.v0_6.Relation;
import org.openstreetmap.osmosis.core.domain.v0_6.RelationMember;
import org.openstreetmap.osmosis.core.domain.v0_6.Tag;
import org.openstreetmap.osmosis.core.domain.v0_6.Way;
import org.openstreetmap.osmosis.core.domain.v0_6.WayNode;
import org.openstreetmap.osmosis.core.task.v0_6.Sink;
import org.openstreetmap.osmosis.xml.common.XmlTimestampFormat;


/**
 * Reads the contents of an osm file using a Stax parser.
 * 
 * @author Jiri Klement
 * @author Brett Henderson
 * @author Igor Podolskiy
 */
public class FastXmlParser {
	
	private static final String ELEMENT_NAME_BOUND = "bound";
	private static final String ELEMENT_NAME_NODE = "node";
	private static final String ELEMENT_NAME_WAY = "way";
	private static final String ELEMENT_NAME_RELATION = "relation";
	private static final String ELEMENT_NAME_TAG = "tag";
	private static final String ELEMENT_NAME_NODE_REFERENCE = "nd";
	private static final String ELEMENT_NAME_MEMBER = "member";
	private static final String ATTRIBUTE_NAME_ID = "id";
	private static final String ATTRIBUTE_NAME_VERSION = "version";
	private static final String ATTRIBUTE_NAME_GENERATOR = "generator";
	private static final String ATTRIBUTE_NAME_TIMESTAMP = "timestamp";
	private static final String ATTRIBUTE_NAME_USER_ID = "uid";
	private static final String ATTRIBUTE_NAME_USER = "user";
	private static final String ATTRIBUTE_NAME_CHANGESET_ID = "changeset";
	private static final String ATTRIBUTE_NAME_LATITUDE = "lat";
	private static final String ATTRIBUTE_NAME_LONGITUDE = "lon";
	private static final String ATTRIBUTE_NAME_KEY = "k";
	private static final String ATTRIBUTE_NAME_VALUE = "v";
	private static final String ATTRIBUTE_NAME_REF = "ref";
	private static final String ATTRIBUTE_NAME_TYPE = "type";
	private static final String ATTRIBUTE_NAME_ROLE = "role";
	private static final String ATTRIBUTE_NAME_BOX = "box";
	private static final String ATTRIBUTE_NAME_ORIGIN = "origin";
	
	private static final Logger LOG = Logger.getLogger(FastXmlParser.class.getName());
	private static final Object ELEMENT_NAME_BOUNDS = "bounds";
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param sink
	 *            The sink receiving all output data.
	 * @param reader
	 *            The input xml reader.
	 * @param enableDateParsing
	 *            If true, parsing of dates in the xml will be enabled,
	 *            otherwise the current system time will be used.
	 */
	public FastXmlParser(Sink sink, XMLStreamReader reader, boolean enableDateParsing) {
		this.sink = sink;
		this.enableDateParsing = enableDateParsing;
		this.reader = reader;
		
		if (enableDateParsing) {
			timestampFormat = new XmlTimestampFormat();
		} else {
			Calendar calendar;
			
			calendar = Calendar.getInstance();
			calendar.set(Calendar.MILLISECOND, 0);
			dummyTimestampContainer = new SimpleTimestampContainer(calendar.getTime());
		}
		
		memberTypeParser = new MemberTypeParser();
	}
	
	private final XMLStreamReader reader;
	private final Sink sink;
	private final boolean enableDateParsing;
	private final MemberTypeParser memberTypeParser;
	private TimestampFormat timestampFormat;
	private TimestampContainer dummyTimestampContainer;

	
	private TimestampContainer parseTimestamp(String data) {
		if (enableDateParsing) {
			return new UnparsedTimestampContainer(timestampFormat, data);
		} else {
			return dummyTimestampContainer;
		}
	}
	
	private void readUnknownElement() throws XMLStreamException {
		int level = 0;
		
		do {
			if (reader.getEventType() == XMLStreamConstants.START_ELEMENT) {
				level++;
			} else if (reader.getEventType() == XMLStreamConstants.END_ELEMENT) {
				level--;
			}
			reader.nextTag();
		} while (level > 0);
	}


	/**
	 * Creates a user instance based on the current entity attributes. This includes identifying the
	 * case where no user is available.
	 * 
	 * @return The appropriate user instance.
	 */
	private OsmUser readUser() {
		String rawUserId;
		String rawUserName;
		
		rawUserId = reader.getAttributeValue(null, ATTRIBUTE_NAME_USER_ID);
		rawUserName = reader.getAttributeValue(null, ATTRIBUTE_NAME_USER);
		
		if (rawUserId != null) {
			int userId;
			String userName;
			
			userId = Integer.parseInt(rawUserId);
			if (rawUserName == null) {
				userName = "";
			} else {
				userName = rawUserName;
			}
			
			return new OsmUser(userId, userName);
			
		} else {
			return OsmUser.NONE;
		}
	}
	
	
	/**
	 * Parses a changeset id from the current entity.
	 * 
	 * @return The changeset id as a long. 0 is returned if no attribute is available.
	 */
	private long readChangesetId() {
		String changesetIdAttribute;
		
		changesetIdAttribute = reader.getAttributeValue(null, ATTRIBUTE_NAME_CHANGESET_ID);
		if (changesetIdAttribute != null) {
			return Long.parseLong(changesetIdAttribute);
		} else {
			return 0;
		}
	}
	
	
	private Bound readBound() throws Exception {
		String boxString;
		String origin;
		String[] boundStrings;
		Double right;
		Double left;
		Double top;
		Double bottom;
		
		boxString = reader.getAttributeValue(null, ATTRIBUTE_NAME_BOX);
		
		if (boxString == null) {
			throw new OsmosisRuntimeException("Missing required box attribute of bound element");
		}
		boundStrings = boxString.split(",");
		if (boundStrings.length != 4) {
			throw new OsmosisRuntimeException("Badly formed box attribute of bound element");
		}
		try {
			bottom = Double.parseDouble(boundStrings[0]);
			left = Double.parseDouble(boundStrings[1]);
			top = Double.parseDouble(boundStrings[2]);
			right = Double.parseDouble(boundStrings[3]);
		} catch (NumberFormatException e) {
			throw new OsmosisRuntimeException("Can't parse box attribute of bound element", e);
		}
		origin = reader.getAttributeValue(null, ATTRIBUTE_NAME_ORIGIN);
		if (origin == null || origin.equals("")) {
			throw new OsmosisRuntimeException("Origin attribute of bound element is empty or missing.");
		}
		Bound bound = new Bound(right, left, top, bottom, origin);
		
		reader.nextTag();
		reader.nextTag();
		
		return bound;
	}
	
	private Bound readBounds(String defaultOrigin) throws Exception {
		double bottom = getRequiredDoubleValue(XmlConstants.ATTRIBUTE_NAME_MINLAT);
		double left = getRequiredDoubleValue(XmlConstants.ATTRIBUTE_NAME_MINLON);
		double top = getRequiredDoubleValue(XmlConstants.ATTRIBUTE_NAME_MAXLAT);
		double right = getRequiredDoubleValue(XmlConstants.ATTRIBUTE_NAME_MAXLON);

		String origin = reader.getAttributeValue(null, ATTRIBUTE_NAME_ORIGIN);
		if (origin == null) {
			origin = defaultOrigin;
		}
		
		reader.nextTag();
		reader.nextTag();

		return new Bound(right, left, top, bottom, origin);
	}
	
	private double getRequiredDoubleValue(String attributeName) {
		String valueString = reader.getAttributeValue(null, attributeName);

		if (valueString == null) {
			throw new OsmosisRuntimeException(String.format(
					"Required attribute %s of the bounds element is missing", attributeName));
		}
		try {
			return Double.parseDouble(valueString);
		} catch (NumberFormatException e) {
			throw new OsmosisRuntimeException(
					String.format("Cannot parse the %s attribute of the bounds element", attributeName), 
					e);
		}
	}
	
	private Tag readTag() throws Exception {
		Tag tag = new Tag(reader.getAttributeValue(null, ATTRIBUTE_NAME_KEY),
				reader.getAttributeValue(null, ATTRIBUTE_NAME_VALUE));
		reader.nextTag();
		reader.nextTag();
		return tag;
	}
	
	private Node readNode() throws Exception {
		long id;
		int version;
		TimestampContainer timestamp;
		OsmUser user;
		long changesetId;
		double latitude;
		double longitude;
		Node node;
		
		id = Long.parseLong(reader.getAttributeValue(null, ATTRIBUTE_NAME_ID));
		version = Integer.parseInt(reader.getAttributeValue(null, ATTRIBUTE_NAME_VERSION));
		timestamp = parseTimestamp(reader.getAttributeValue(null, ATTRIBUTE_NAME_TIMESTAMP));
		changesetId = Long.parseLong(reader.getAttributeValue(null, ATTRIBUTE_NAME_CHANGESET_ID));
		user = readUser();
		changesetId = readChangesetId();
		latitude = Double.parseDouble(reader.getAttributeValue(null, ATTRIBUTE_NAME_LATITUDE));
		longitude = Double.parseDouble(reader.getAttributeValue(null, ATTRIBUTE_NAME_LONGITUDE));
		
		node = new Node(new CommonEntityData(id, version, timestamp, user, changesetId), latitude, longitude);
		
		reader.nextTag();
		while (reader.getEventType() == XMLStreamConstants.START_ELEMENT) {
			if (reader.getLocalName().equals(ELEMENT_NAME_TAG)) {
				node.getTags().add(readTag());
			} else {
				readUnknownElement();
			}
		}
		
		reader.nextTag();
		
		return node;
	}
	
	private WayNode readWayNode() throws Exception {
		WayNode node = new WayNode(
				Long.parseLong(reader.getAttributeValue(null, ATTRIBUTE_NAME_REF)));
		reader.nextTag();
		reader.nextTag();
		return node;
	}
	
	private Way readWay() throws Exception {
		long id;
		int version;
		TimestampContainer timestamp;
		OsmUser user;
		long changesetId;
		Way way;
		
		id = Long.parseLong(reader.getAttributeValue(null, ATTRIBUTE_NAME_ID));
		version = Integer.parseInt(reader.getAttributeValue(null, ATTRIBUTE_NAME_VERSION));
		timestamp = parseTimestamp(reader.getAttributeValue(null, ATTRIBUTE_NAME_TIMESTAMP));
		user = readUser();
		changesetId = readChangesetId();
		
		way = new Way(new CommonEntityData(id, version, timestamp, user, changesetId));
		
		reader.nextTag();
		while (reader.getEventType() == XMLStreamConstants.START_ELEMENT) {
			if (reader.getLocalName().equals(ELEMENT_NAME_TAG)) {
				way.getTags().add(readTag());
			} else if (reader.getLocalName().equals(ELEMENT_NAME_NODE_REFERENCE)) {
				way.getWayNodes().add(readWayNode());
			} else {
				readUnknownElement();
			}
		}
		reader.nextTag();

		return way;
	}
	
	private RelationMember readRelationMember() throws Exception {
		long id;
		EntityType type;
		String role;
		
		id = Long.parseLong(reader.getAttributeValue(null, ATTRIBUTE_NAME_REF));
		type = memberTypeParser.parse(reader.getAttributeValue(null, ATTRIBUTE_NAME_TYPE));
		role = reader.getAttributeValue(null, ATTRIBUTE_NAME_ROLE);
		
		RelationMember relationMember = new RelationMember(id, type, role);
		
		reader.nextTag();
		reader.nextTag();
		
		return relationMember;
	}
	
	private Relation readRelation() throws Exception {
		long id;
		int version;
		TimestampContainer timestamp;
		OsmUser user;
		long changesetId;
		Relation relation;
		
		id = Long.parseLong(reader.getAttributeValue(null, ATTRIBUTE_NAME_ID));
		version = Integer.parseInt(reader.getAttributeValue(null, ATTRIBUTE_NAME_VERSION));
		timestamp = parseTimestamp(reader.getAttributeValue(null, ATTRIBUTE_NAME_TIMESTAMP));
		user = readUser();
		changesetId = readChangesetId();
		
		relation = new Relation(new CommonEntityData(id, version, timestamp, user, changesetId));
		
		reader.nextTag();
		while (reader.getEventType() == XMLStreamConstants.START_ELEMENT) {
			if (reader.getLocalName().equals(ELEMENT_NAME_TAG)) {
				relation.getTags().add(readTag());
			} else if (reader.getLocalName().equals(ELEMENT_NAME_MEMBER)) {
				relation.getMembers().add(readRelationMember());
			} else {
				readUnknownElement();
			}
		}
		reader.nextTag();
		
		return relation;
	}

	
	/**
	 * Parses the xml and sends all data to the sink.
	 */
	public void readOsm() {
		
		try {
		
			String generator = null;
			
			if (reader.nextTag() == XMLStreamConstants.START_ELEMENT && reader.getLocalName().equals("osm")) {

				String fileVersion;

				fileVersion = reader.getAttributeValue(null, ATTRIBUTE_NAME_VERSION);

				if (!XmlConstants.OSM_VERSION.equals(fileVersion)) {
					LOG.warning(
							"Expected version " + XmlConstants.OSM_VERSION
							+ " but received " + fileVersion + "."
					);
				}
				
				generator = reader.getAttributeValue(null, ATTRIBUTE_NAME_GENERATOR);

				reader.nextTag();
				

				if (reader.getEventType() == XMLStreamConstants.START_ELEMENT
						&& reader.getLocalName().equals(ELEMENT_NAME_BOUND)) {
					LOG.fine("Legacy <bound> element encountered.");
					sink.process(new BoundContainer(readBound()));
				}
				
				if (reader.getEventType() == XMLStreamConstants.START_ELEMENT
						&& reader.getLocalName().equals(ELEMENT_NAME_BOUNDS)) {
					sink.process(new BoundContainer(readBounds(generator)));
				}

				while (reader.getEventType() == XMLStreamConstants.START_ELEMENT) {			
					// Node, way, relation
					if (reader.getLocalName().equals(ELEMENT_NAME_NODE)) {
						sink.process(new NodeContainer(readNode()));
					} else if (reader.getLocalName().equals(ELEMENT_NAME_WAY)) {
						sink.process(new WayContainer(readWay()));
					} else if (reader.getLocalName().equals(ELEMENT_NAME_RELATION)) {
						sink.process(new RelationContainer(readRelation()));
					} else {
						readUnknownElement();
					}
				}

			} else {
				throw new XMLStreamException();
			}
		} catch (Exception e) {
			throw new OsmosisRuntimeException(e);
		}
	}
}
