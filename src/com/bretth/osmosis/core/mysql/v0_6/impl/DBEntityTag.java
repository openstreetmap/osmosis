// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package com.bretth.osmosis.core.mysql.v0_6.impl;

import com.bretth.osmosis.core.domain.v0_6.Tag;
import com.bretth.osmosis.core.store.StoreClassRegister;
import com.bretth.osmosis.core.store.StoreReader;
import com.bretth.osmosis.core.store.StoreWriter;
import com.bretth.osmosis.core.store.Storeable;


/**
 * A data class for representing an entity tag database record. This extends a
 * tag with fields relating it to the owning entity.
 * 
 * @author Brett Henderson
 */
public class DBEntityTag implements Storeable {
	
	private long entityId;
	private Tag tag;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param entityId
	 *            The owning entity id.
	 * @param tag
	 *            The tag to be wrapped.
	 */
	public DBEntityTag(long entityId, Tag tag) {
		this.entityId = entityId;
		this.tag = tag;
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
	public DBEntityTag(StoreReader sr, StoreClassRegister scr) {
		this(
			sr.readLong(),
			new Tag(sr, scr)
		);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void store(StoreWriter sw, StoreClassRegister scr) {
		sw.writeLong(entityId);
		tag.store(sw, scr);
	}
	
	
	/**
	 * @return The entity id.
	 */
	public long getEntityId() {
		return entityId;
	}
	
	
	/**
	 * @return The tag.
	 */
	public Tag getTag() {
		return tag;
	}
}
