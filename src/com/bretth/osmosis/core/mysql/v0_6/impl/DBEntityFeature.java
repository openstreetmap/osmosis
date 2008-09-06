// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package com.bretth.osmosis.core.mysql.v0_6.impl;

import com.bretth.osmosis.core.store.GenericObjectReader;
import com.bretth.osmosis.core.store.GenericObjectWriter;
import com.bretth.osmosis.core.store.StoreClassRegister;
import com.bretth.osmosis.core.store.StoreReader;
import com.bretth.osmosis.core.store.StoreWriter;
import com.bretth.osmosis.core.store.Storeable;


/**
 * A data class for representing a database record for an entity feature. This
 * aggregates a standard entity feature type with a field relating it to the
 * owning entity.
 * 
 * @author Brett Henderson
 * @param <T>
 *            The feature type to be encapsulated.
 */
public class DBEntityFeature<T extends Storeable> implements Storeable {
	
	private long entityId;
	private T entityFeature;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param entityId
	 *            The owning entity id.
	 * @param entityFeature
	 *            The way node being referenced.
	 */
	public DBEntityFeature(long entityId, T entityFeature) {
		this.entityId = entityId;
		this.entityFeature = entityFeature;
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
	@SuppressWarnings("unchecked")
	public DBEntityFeature(StoreReader sr, StoreClassRegister scr) {
		this(
			sr.readLong(),
			(T) new GenericObjectReader(sr, scr).readObject()
		);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void store(StoreWriter sw, StoreClassRegister scr) {
		sw.writeLong(entityId);
		new GenericObjectWriter(sw, scr).writeObject(entityFeature);
	}
	
	
	/**
	 * @return The entity id.
	 */
	public long getEntityId() {
		return entityId;
	}
	
	
	/**
	 * @return The entity feature.
	 */
	public T getEntityFeature() {
		return entityFeature;
	}
}
