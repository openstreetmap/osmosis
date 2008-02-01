// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package com.bretth.osmosis.core.mysql.v0_5.impl;

import java.util.HashMap;
import java.util.Map;

import com.bretth.osmosis.core.OsmosisRuntimeException;
import com.bretth.osmosis.core.domain.v0_5.EntityType;


/**
 * Parses the database representation of a relation member type into an entity
 * type object.
 * 
 * @author Brett Henderson
 */
public class MemberTypeParser {
	
	private static final Map<String, EntityType> memberTypeMap = new HashMap<String, EntityType>();
	
	static {
		memberTypeMap.put("node", EntityType.Node);
		memberTypeMap.put("way", EntityType.Way);
		memberTypeMap.put("relation", EntityType.Relation);
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
		if (memberTypeMap.containsKey(memberType)) {
			return memberTypeMap.get(memberType);
		} else {
			throw new OsmosisRuntimeException("The member type " + memberType + " is not recognised.");
		}
	}
}
