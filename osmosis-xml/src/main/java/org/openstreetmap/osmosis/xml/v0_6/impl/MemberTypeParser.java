// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.xml.v0_6.impl;

import java.util.HashMap;
import java.util.Map;

import org.openstreetmap.osmosis.core.OsmosisRuntimeException;
import org.openstreetmap.osmosis.core.domain.v0_6.EntityType;


/**
 * Parses the xml representation of a relation member type into an entity type
 * object.
 * 
 * @author Brett Henderson
 */
public class MemberTypeParser {
	
	private static final Map<String, EntityType> MEMBER_TYPE_MAP = new HashMap<String, EntityType>();
	
	static {
		MEMBER_TYPE_MAP.put("node", EntityType.Node);
		MEMBER_TYPE_MAP.put("way", EntityType.Way);
		MEMBER_TYPE_MAP.put("relation", EntityType.Relation);
	}
	
	
	/**
	 * Parses the database representation of a relation member type into an
	 * entity type object.
	 * 
	 * @param memberType
	 *            The database value of member type.
	 * @return A strongly typed entity type.
	 */
	public EntityType parse(String memberType) {
		if (MEMBER_TYPE_MAP.containsKey(memberType)) {
			return MEMBER_TYPE_MAP.get(memberType);
		} else {
			throw new OsmosisRuntimeException("The member type " + memberType + " is not recognised.");
		}
	}
}
