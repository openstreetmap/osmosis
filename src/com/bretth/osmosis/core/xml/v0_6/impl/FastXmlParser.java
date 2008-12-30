package com.bretth.osmosis.core.xml.v0_6.impl;

import java.util.Calendar;
import java.util.logging.Logger;

import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import com.bretth.osmosis.core.OsmosisRuntimeException;
import com.bretth.osmosis.core.container.v0_6.BoundContainer;
import com.bretth.osmosis.core.container.v0_6.NodeContainer;
import com.bretth.osmosis.core.container.v0_6.RelationContainer;
import com.bretth.osmosis.core.container.v0_6.WayContainer;
import com.bretth.osmosis.core.domain.common.SimpleTimestampContainer;
import com.bretth.osmosis.core.domain.common.TimestampContainer;
import com.bretth.osmosis.core.domain.common.TimestampFormat;
import com.bretth.osmosis.core.domain.common.UnparsedTimestampContainer;
import com.bretth.osmosis.core.domain.v0_6.Bound;
import com.bretth.osmosis.core.domain.v0_6.EntityType;
import com.bretth.osmosis.core.domain.v0_6.Node;
import com.bretth.osmosis.core.domain.v0_6.OsmUser;
import com.bretth.osmosis.core.domain.v0_6.Relation;
import com.bretth.osmosis.core.domain.v0_6.RelationMember;
import com.bretth.osmosis.core.domain.v0_6.Tag;
import com.bretth.osmosis.core.domain.v0_6.Way;
import com.bretth.osmosis.core.domain.v0_6.WayNode;
import com.bretth.osmosis.core.task.v0_6.Sink;
import com.bretth.osmosis.core.xml.common.XmlTimestampFormat;


/**
 * Reads the contents of an osm file using a Stax parser.
 * 
 * @author Jiri Klement
 * @author Brett Henderson
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
	private static final String ATTRIBUTE_NAME_TIMESTAMP = "timestamp";
	private static final String ATTRIBUTE_NAME_USER_ID = "uid";
	private static final String ATTRIBUTE_NAME_USER = "user";
	private static final String ATTRIBUTE_NAME_LATITUDE = "lat";
	private static final String ATTRIBUTE_NAME_LONGITUDE = "lon";
	private static final String ATTRIBUTE_NAME_KEY = "k";
	private static final String ATTRIBUTE_NAME_VALUE = "v";
	private static final String ATTRIBUTE_NAME_REF = "ref";
	private static final String ATTRIBUTE_NAME_TYPE = "type";
	private static final String ATTRIBUTE_NAME_ROLE = "role";
	private static final String ATTRIBUTE_NAME_BOX = "box";
	private static final String ATTRIBUTE_NAME_ORIGIN = "origin";
	
	private static final Logger log = Logger.getLogger(FastXmlParser.class.getName());
	
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

		Location l = reader.getLocation();
		log.warning(String.format(
				"Unknown xml element %s. publicId=(%s), systemId=(%s), " + 
				"lineNumber=%d, columnNumber=%d", 
				reader.getName(), l.getPublicId(), l.getSystemId(), l.getLineNumber(), l.getColumnNumber()));
		
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
	 * Creates a user instance appropriate to the arguments. This includes
	 * identifying the case where no user is available.
	 * 
	 * @param rawUserId
	 *            The value of the user id attribute.
	 * @param rawUserName
	 *            The value of the user name attribute.
	 * @return The appropriate user instance.
	 */
	private OsmUser readUser(String rawUserId, String rawUserName) {
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
		double latitude;
		double longitude;
		
		id = Long.parseLong(reader.getAttributeValue(null, ATTRIBUTE_NAME_ID));
		version = Integer.parseInt(reader.getAttributeValue(null, ATTRIBUTE_NAME_VERSION));
		timestamp = parseTimestamp(reader.getAttributeValue(null, ATTRIBUTE_NAME_TIMESTAMP));
		user = readUser(
			reader.getAttributeValue(null, ATTRIBUTE_NAME_USER_ID),
			reader.getAttributeValue(null, ATTRIBUTE_NAME_USER)
		);
		latitude = Double.parseDouble(reader.getAttributeValue(null, ATTRIBUTE_NAME_LATITUDE));
		longitude = Double.parseDouble(reader.getAttributeValue(null, ATTRIBUTE_NAME_LONGITUDE));
		
		Node node = new Node(id, version, timestamp, user, latitude, longitude);
		
		reader.nextTag();
		while (reader.getEventType() == XMLStreamConstants.START_ELEMENT) {
			if (reader.getLocalName().equals(ELEMENT_NAME_TAG)) {
				node.addTag(readTag());
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
		
		id = Long.parseLong(reader.getAttributeValue(null, ATTRIBUTE_NAME_ID));
		version = Integer.parseInt(reader.getAttributeValue(null, ATTRIBUTE_NAME_VERSION));
		timestamp = parseTimestamp(reader.getAttributeValue(null, ATTRIBUTE_NAME_TIMESTAMP));
		user = readUser(
			reader.getAttributeValue(null, ATTRIBUTE_NAME_USER_ID),
			reader.getAttributeValue(null, ATTRIBUTE_NAME_USER)
		);
		
		Way way = new Way(id, version, timestamp, user);
		
		reader.nextTag();
		while (reader.getEventType() == XMLStreamConstants.START_ELEMENT) {
			if (reader.getLocalName().equals(ELEMENT_NAME_TAG)) {
				way.addTag(readTag());
			} else if (reader.getLocalName().equals(ELEMENT_NAME_NODE_REFERENCE)) {
				way.addWayNode(readWayNode());
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
		
		id = Long.parseLong(reader.getAttributeValue(null, ATTRIBUTE_NAME_ID));
		version = Integer.parseInt(reader.getAttributeValue(null, ATTRIBUTE_NAME_VERSION));
		timestamp = parseTimestamp(reader.getAttributeValue(null, ATTRIBUTE_NAME_TIMESTAMP));
		user = readUser(
			reader.getAttributeValue(null, ATTRIBUTE_NAME_USER_ID),
			reader.getAttributeValue(null, ATTRIBUTE_NAME_USER)
		);
		
		Relation relation = new Relation(id, version, timestamp, user);
		
		reader.nextTag();
		while (reader.getEventType() == XMLStreamConstants.START_ELEMENT) {
			if (reader.getLocalName().equals(ELEMENT_NAME_TAG)) {
				relation.addTag(readTag());
			} else if (reader.getLocalName().equals(ELEMENT_NAME_MEMBER)) {
				relation.addMember(readRelationMember());
			} else {
				readUnknownElement();
			}
		}
		reader.nextTag();
		
		return relation;
	}

	public void readOsm() {
		
		try {
		
			if (reader.nextTag() == XMLStreamConstants.START_ELEMENT && reader.getLocalName().equals("osm")) {

				String fileVersion;

				fileVersion = reader.getAttributeValue(null, ATTRIBUTE_NAME_VERSION);

				if (!XmlConstants.OSM_VERSION.equals(fileVersion)) {
					log.warning(
							"Expected version " + XmlConstants.OSM_VERSION
							+ " but received " + fileVersion + "."
					);
				}

				reader.nextTag();
				

				if (reader.getEventType() == XMLStreamConstants.START_ELEMENT
						&& reader.getLocalName().equals(ELEMENT_NAME_BOUND)) {
					sink.process(new BoundContainer(readBound()));
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
