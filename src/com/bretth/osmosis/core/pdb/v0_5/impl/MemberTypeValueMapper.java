// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package com.bretth.osmosis.core.pdb.v0_5.impl;

import java.util.HashMap;
import java.util.Map;

import com.bretth.osmosis.core.OsmosisRuntimeException;
import com.bretth.osmosis.core.domain.v0_5.EntityType;

/**
 * This is a utility class for mapping between relation member type columns and
 * the corresponding entity type.
 * 
 * @author Brett Henderson
 */
public class MemberTypeValueMapper {
	
	private static Map<EntityType, Byte> entityToMemberMap;
	private static Map<Byte, EntityType> memberToEntityMap;
	
	static {
		EntityType[] entityTypes;
		
		entityTypes = EntityType.values();
		
		entityToMemberMap = new HashMap<EntityType, Byte>(entityTypes.length);
		memberToEntityMap = new HashMap<Byte, EntityType>(entityTypes.length);
		
		for (byte memberType = 0; memberType < entityTypes.length; memberType++) {
			EntityType entityType;
			
			entityType = entityTypes[memberType];
			
			entityToMemberMap.put(entityType, memberType);
			memberToEntityMap.put(memberType, entityType);
		}
	}
	
	
	/**
	 * Returns the member type value corresponding to the specified entity type.
	 * 
	 * @param entityType
	 *            The entity type.
	 * @return The corresponding member type value.
	 */
	public byte getMemberType(EntityType entityType) {
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
	public EntityType getEntityType(byte memberType) {
		if (memberToEntityMap.containsKey(memberType)) {
			return memberToEntityMap.get(memberType);
		} else {
			throw new OsmosisRuntimeException("The member type " + (int) memberType + " is not recognised.");
		}
	}
}
