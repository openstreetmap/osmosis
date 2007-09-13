package com.bretth.osmosis.core.mysql.v0_5.impl;

import com.bretth.osmosis.core.domain.v0_5.EntityType;
import com.bretth.osmosis.core.domain.v0_5.RelationMember;


/**
 * A data class for representing a relation member database record. This extends
 * a relation member with fields relating it to the owning relation.
 * 
 * @author Brett Henderson
 */
public class DBRelationMember extends RelationMember {
	private static final long serialVersionUID = 1L;
	
	
	private long relationId;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param relationId
	 *            The owning relation id.
	 * @param memberId
	 *            The id of the entity that this member consists of.
	 * @param memberType
	 *            The type of the entity that this member consists of.
	 * @param memberRole
	 *            The role that this member forms within the relation.
	 */
	public DBRelationMember(long relationId, long memberId, EntityType memberType, String memberRole) {
		super(memberId, memberType, memberRole);
		
		this.relationId = relationId;
	}
	
	
	/**
	 * @return The relation id.
	 */
	public long getRelationId() {
		return relationId;
	}
}
