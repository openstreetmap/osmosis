// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package org.openstreetmap.osmosis.core.mysql.v0_5.impl;

import java.util.HashMap;
import java.util.Map;

import org.openstreetmap.osmosis.core.OsmosisRuntimeException;
import org.openstreetmap.osmosis.core.domain.v0_5.EntityType;


/**
 * Renders a member type object into its database representation.
 * 
 * @author Brett Henderson
 */
public class MemberTypeRenderer {
	
	private static final Map<EntityType, String> memberTypeMap = new HashMap<EntityType, String>();
	
	static {
		memberTypeMap.put(EntityType.Node, "node");
		memberTypeMap.put(EntityType.Way, "way");
		memberTypeMap.put(EntityType.Relation, "relation");
	}
	
	
	/**
	 * Renders a member type into its xml representation.
	 * 
	 * @param memberType
	 *            The member type.
	 * @return A rendered member type.
	 */
	public String render(EntityType memberType) {
		if (memberTypeMap.containsKey(memberType)) {
			return memberTypeMap.get(memberType);
		} else {
			throw new OsmosisRuntimeException("The member type " + memberType + " is not recognised.");
		}
	}
}
