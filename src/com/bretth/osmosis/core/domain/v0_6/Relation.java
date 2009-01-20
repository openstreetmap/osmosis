// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package com.bretth.osmosis.core.domain.v0_6;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.Iterator;
import java.util.List;

import com.bretth.osmosis.core.domain.common.SimpleTimestampContainer;
import com.bretth.osmosis.core.domain.common.TimestampContainer;
import com.bretth.osmosis.core.store.StoreClassRegister;
import com.bretth.osmosis.core.store.StoreReader;
import com.bretth.osmosis.core.store.StoreWriter;
import com.bretth.osmosis.core.util.IntAsChar;


/**
 * A data class representing a single OSM relation.
 * 
 * @author Brett Henderson
 */
public class Relation extends Entity implements Comparable<Relation> {
	private static final long serialVersionUID = 1L;
	
	
	private List<RelationMember> members;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param id
	 *            The unique identifier.
	 * @param version
	 *            The version of the entity.
	 * @param timestamp
	 *            The last updated timestamp.
	 * @param user
	 *            The user that last modified this entity.
	 * @param tags
	 *            The tags to apply to the object.
	 * @param members
	 *            The members to apply to the object.
	 */
	public Relation(long id, int version, Date timestamp, OsmUser user, Collection<Tag> tags, List<RelationMember> members) {
		// Chain to the more-specific constructor
		this(id, version, new SimpleTimestampContainer(timestamp), user, tags, members);
	}
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param id
	 *            The unique identifier.
	 * @param version
	 *            The version of the entity.
	 * @param timestampContainer
	 *            The container holding the timestamp in an alternative
	 *            timestamp representation.
	 * @param user
	 *            The user that last modified this entity.
	 * @param tags
	 *            The tags to apply to the object.
	 * @param members
	 *            The members to apply to the object.
	 */
	public Relation(long id, int version, TimestampContainer timestampContainer, OsmUser user, Collection<Tag> tags, List<RelationMember> members) {
		super(id, timestampContainer, user, version, tags);
		
		this.members = Collections.unmodifiableList(new ArrayList<RelationMember>(members));
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
	public Relation(StoreReader sr, StoreClassRegister scr) {
		super(sr, scr);
		
		List<RelationMember> tmpMembers;
		int featureCount;
		
		featureCount = sr.readCharacter();
		
		tmpMembers = new ArrayList<RelationMember>();
		for (int i = 0; i < featureCount; i++) {
			tmpMembers.add(new RelationMember(sr, scr));
		}
		members = Collections.unmodifiableList(tmpMembers);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void store(StoreWriter sw, StoreClassRegister scr) {
		super.store(sw, scr);
		
		sw.writeCharacter(IntAsChar.intToChar(members.size()));
		for (RelationMember relationMember : members) {
			relationMember.store(sw, scr);
		}
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public EntityType getType() {
		return EntityType.Relation;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object o) {
		if (o instanceof Relation) {
			return compareTo((Relation) o) == 0;
		} else {
			return false;
		}
	}
	
	
	/**
	 * Compares this member list to the specified member list. The bigger list
	 * is considered bigger, if that is equal then each relation member is
	 * compared.
	 * 
	 * @param comparisonMemberList
	 *            The member list to compare to.
	 * @return 0 if equal, <0 if considered "smaller", and >0 if considered
	 *         "bigger".
	 */
	protected int compareMemberList(Collection<RelationMember> comparisonMemberList) {
		Iterator<RelationMember> i;
		Iterator<RelationMember> j;
		
		// The list with the most entities is considered bigger.
		if (members.size() != comparisonMemberList.size()) {
			return members.size() - comparisonMemberList.size();
		}
		
		// Check the individual node references.
		i = members.iterator();
		j = comparisonMemberList.iterator();
		while (i.hasNext()) {
			int result = i.next().compareTo(j.next());
			
			if (result != 0) {
				return result;
			}
		}
		
		// There are no differences.
		return 0;
	}


	/**
	 * Compares this relation to the specified relation. The relation comparison
	 * is based on a comparison of id, version, timestamp, and tags in that order.
	 * 
	 * @param comparisonRelation
	 *            The relation to compare to.
	 * @return 0 if equal, <0 if considered "smaller", and >0 if considered
	 *         "bigger".
	 */
	public int compareTo(Relation comparisonRelation) {
		int memberListResult;
		
		if (this.getId() < comparisonRelation.getId()) {
			return -1;
		}
		if (this.getId() > comparisonRelation.getId()) {
			return 1;
		}

		if (this.getVersion() < comparisonRelation.getVersion()) {
			return -1;
		}
		if (this.getVersion() > comparisonRelation.getVersion()) {
			return 1;
		}

		if (this.getTimestamp() == null && comparisonRelation.getTimestamp() != null) {
			return -1;
		}
		if (this.getTimestamp() != null && comparisonRelation.getTimestamp() == null) {
			return 1;
		}
		if (this.getTimestamp() != null && comparisonRelation.getTimestamp() != null) {
			int result;
			
			result = this.getTimestamp().compareTo(comparisonRelation.getTimestamp());
			
			if (result != 0) {
				return result;
			}
		}
		
		memberListResult = compareMemberList(
			comparisonRelation.members
		);
		
		if (memberListResult != 0) {
			return memberListResult;
		}
		
		return compareTags(comparisonRelation.getTags());
	}
	
	
	/**
	 * Returns the attached list of relation members. The returned list is
	 * read-only.
	 * 
	 * @return The member list.
	 */
	public List<RelationMember> getMembers() {
		return members;
	}

	/**
	 * Returns a list of all relation members that have the given
	 * role. Modifying the returned list has no effect on this
	 * relation's member-list.
	 * 
	 * @param aRole the role to search for. Not null and case-sensitive.
	 * @return The member list.
	 */
	public List<RelationMember> getMembersByRole(final String aRole) {
		if (aRole == null) {
			throw new IllegalArgumentException("Null role given.");
		}
		List<RelationMember> retval = new LinkedList<RelationMember>();
		for (RelationMember relationMember : this.members) {
			if (relationMember.getMemberRole() != null
				&& relationMember.getMemberRole().equals(aRole)) {
				retval.add(relationMember);
			}
		}
		return retval;
	}

	/**
	 * Adds a new member.
	 * 
	 * @param member
	 *            The member to add.
	 */
	public void addMember(final RelationMember member) {
		members.add(member);
	}

	/**
	 * Create a new relation with the given member removed.
	 * 
	 * @param member
	 *            The member to remove.
	 * @return a relation identical to this but without the given member
	 */
	public Relation removeMember(final RelationMember member) {
		ArrayList<RelationMember> newMembers = new ArrayList<RelationMember>(getMembers());
		newMembers.remove(member);
		Relation newRelation = new Relation(getId(), getVersion(), getTimestamp(), getUser(), getTags(), newMembers);
		newRelation.addMembers(newMembers);
		return newRelation;
	}


	/**
	 * Adds all members in the collection to the relation.
	 * 
	 * @param members
	 *            The collection of members to be added.
	 */
	public void addMembers(Collection<RelationMember> members) {
		this.members.addAll(members);
	}
}
