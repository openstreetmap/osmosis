// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.xml.v0_6.impl;

import java.util.HashMap;
import java.util.Map;

import org.openstreetmap.osmosis.core.OsmosisRuntimeException;
import org.openstreetmap.osmosis.core.domain.v0_6.EntityType;


/**
 * Renders a member type object into its xml representation.
 * 
 * @author Brett Henderson
 */
public class MemberTypeRenderer {
	
	private static final Map<EntityType, String> MEMBER_TYPE_MAP = new HashMap<EntityType, String>();
	
	static {
		MEMBER_TYPE_MAP.put(EntityType.Node, "node");
		MEMBER_TYPE_MAP.put(EntityType.Way, "way");
		MEMBER_TYPE_MAP.put(EntityType.Relation, "relation");
	}
	
	
	/**
	 * Renders a member type into its xml representation.
	 * 
	 * @param memberType
	 *            The member type.
	 * @return A rendered member type.
	 */
	public String render(EntityType memberType) {
		if (MEMBER_TYPE_MAP.containsKey(memberType)) {
			return MEMBER_TYPE_MAP.get(memberType);
		} else {
			throw new OsmosisRuntimeException("The member type " + memberType + " is not recognised.");
		}
	}
}
