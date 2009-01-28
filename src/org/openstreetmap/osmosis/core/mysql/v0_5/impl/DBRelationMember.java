// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package org.openstreetmap.osmosis.core.mysql.v0_5.impl;

import org.openstreetmap.osmosis.core.domain.v0_5.RelationMember;
import org.openstreetmap.osmosis.core.store.StoreClassRegister;
import org.openstreetmap.osmosis.core.store.StoreReader;
import org.openstreetmap.osmosis.core.store.StoreWriter;
import org.openstreetmap.osmosis.core.store.Storeable;


/**
 * A data class for representing a relation member database record. This
 * incorporates a relation member with fields relating it to the owning
 * relation.
 * 
 * @author Brett Henderson
 */
public class DBRelationMember implements Storeable {
	
	private long relationId;
	private RelationMember relationMember;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param relationId
	 *            The owning relation id.
	 * @param relationMember
	 *            The relation member.
	 */
	public DBRelationMember(long relationId, RelationMember relationMember) {
		this.relationId = relationId;
		this.relationMember = relationMember;
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
	public DBRelationMember(StoreReader sr, StoreClassRegister scr) {
		this(
			sr.readLong(),
			new RelationMember(sr, scr)
		);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void store(StoreWriter sw, StoreClassRegister scr) {
		sw.writeLong(relationId);
		relationMember.store(sw, scr);
	}
	
	
	/**
	 * @return The relation id.
	 */
	public long getRelationId() {
		return relationId;
	}
	
	
	/**
	 * @return The relation member.
	 */
	public RelationMember getRelationMember() {
		return relationMember;
	}
}
