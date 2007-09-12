package com.bretth.osmosis.core.domain.v0_5;

import java.io.Serializable;


/**
 * A data class representing a single member within a relation entity.
 * 
 * @author Brett Henderson
 */
public class RelationMember implements Comparable<RelationMember>, Serializable {
	private static final long serialVersionUID = 1L;
	
	private long memberId;
	private EntityType memberType;
	private String memberRole;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param memberId
	 *            The id of the entity that this member consists of.
	 * @param memberType
	 *            The type of the entity that this member consists of.
	 * @param memberRole
	 *            The role that this member forms within the relation.
	 */
	public RelationMember(long memberId, EntityType memberType, String memberRole) {
		this.memberId = memberId;
		this.memberType = memberType;
		this.memberRole = memberRole;
	}
	
	
	/**
	 * Compares this relation member to the specified relation member. The
	 * relation member comparison is based on a comparison of member type, then
	 * member id, then role.
	 * 
	 * @param relationMember
	 *            The relation member to compare to.
	 * @return 0 if equal, <0 if considered "smaller", and >0 if considered
	 *         "bigger".
	 */
	public int compareTo(RelationMember relationMember) {
		long result;
		
		// Compare the member type.
		result = this.memberType.compareTo(relationMember.memberType);
		if (result > 0) {
			return 1;
		} else if (result < 0) {
			return -1;
		}
		
		// Compare the member id.
		result = this.memberId - relationMember.memberId;
		if (result > 0) {
			return 1;
		} else if (result < 0) {
			return -1;
		}
		
		// Compare the member role.
		result = this.memberRole.compareTo(relationMember.memberRole);
		if (result > 0) {
			return 1;
		} else if (result < 0) {
			return -1;
		}
		
		// No differences detected.
		return 0;
	}
	
	
	/**
	 * Returns the id of the member entity.
	 * 
	 * @return The member id.
	 */
	public long getMemberId() {
		return memberId;
	}
	
	
	/**
	 * Returns the type of the member entity.
	 * 
	 * @return The member type.
	 */
	public EntityType getMemberType() {
		return memberType;
	}
	
	
	/**
	 * Returns the role that this member forms within the relation.
	 * 
	 * @return The role.
	 */
	public String getMemberRole() {
		return memberRole;
	}
}
