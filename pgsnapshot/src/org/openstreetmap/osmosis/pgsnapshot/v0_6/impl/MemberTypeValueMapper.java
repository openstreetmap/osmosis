// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.pgsnapshot.v0_6.impl;

import java.util.HashMap;
import java.util.Map;

import org.openstreetmap.osmosis.core.OsmosisRuntimeException;
import org.openstreetmap.osmosis.core.domain.v0_6.EntityType;


/**
 * This is a utility class for mapping between relation member type columns and
 * the corresponding entity type.
 * 
 * @author Brett Henderson
 */
public class MemberTypeValueMapper {
	
	private static Map<EntityType, String> entityToMemberMap;
	private static Map<String, EntityType> memberToEntityMap;
	
	
	private static void addEntityTypeMapping(EntityType entityType, String memberType) {
		if (entityToMemberMap.containsKey(entityType)) {
			throw new OsmosisRuntimeException("Entity type (" + entityType + ") already has a mapping.");
		}
		
		entityToMemberMap.put(entityType, memberType);
		memberToEntityMap.put(memberType, entityType);
	}
	
	
	static {
		EntityType[] entityTypes;
		
		entityTypes = EntityType.values();
		
		entityToMemberMap = new HashMap<EntityType, String>(entityTypes.length);
		memberToEntityMap = new HashMap<String, EntityType>(entityTypes.length);
		
		addEntityTypeMapping(EntityType.Bound, "B");
		addEntityTypeMapping(EntityType.Node, "N");
		addEntityTypeMapping(EntityType.Way, "W");
		addEntityTypeMapping(EntityType.Relation, "R");
	}
	
	
	/**
	 * Returns the member type value corresponding to the specified entity type.
	 * 
	 * @param entityType
	 *            The entity type.
	 * @return The corresponding member type value.
	 */
	public String getMemberType(EntityType entityType) {
		if (entityToMemberMap.containsKey(entityType)) {
			return entityToMemberMap.get(entityType);
		} else {
			throw new OsmosisRuntimeException("The entity type " + entityType + " is not recognised.");
		}
	}
	
	
	/**
	 * Returns the entity type value corresponding to the specified member type.
	 * 
	 * @param memberType
	 *            The member type.
	 * @return The corresponding entity type value.
	 */
	public EntityType getEntityType(String memberType) {
		if (memberToEntityMap.containsKey(memberType)) {
			return memberToEntityMap.get(memberType);
		} else {
			throw new OsmosisRuntimeException("The member type " + memberType + " is not recognised.");
		}
	}
}
