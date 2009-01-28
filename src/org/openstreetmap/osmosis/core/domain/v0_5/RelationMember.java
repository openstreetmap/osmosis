// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package org.openstreetmap.osmosis.core.domain.v0_5;

import org.openstreetmap.osmosis.core.store.StoreClassRegister;
import org.openstreetmap.osmosis.core.store.StoreReader;
import org.openstreetmap.osmosis.core.store.StoreWriter;
import org.openstreetmap.osmosis.core.store.Storeable;
import org.openstreetmap.osmosis.core.util.LongAsInt;


/**
 * A data class representing a single member within a relation entity.
 * 
 * @author Brett Henderson
 */
public class RelationMember implements Comparable<RelationMember>, Storeable {
	
	private int memberId;
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
		this.memberId = LongAsInt.longToInt(memberId);
		this.memberType = memberType;
		this.memberRole = memberRole;
	}
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param sr
	 *            The store to read state from.
	 * @param scr
	 *            Maintains the mapping between classes and their identifiers
	 *            within the store.
	 */
	public RelationMember(StoreReader sr, StoreClassRegister scr) {
		this(
			sr.readInteger(),
			EntityType.valueOf(sr.readString()),
			sr.readString()
		);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void store(StoreWriter sw, StoreClassRegister scr) {
		sw.writeInteger(memberId);
		sw.writeString(memberType.toString());
		sw.writeString(memberRole);
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
