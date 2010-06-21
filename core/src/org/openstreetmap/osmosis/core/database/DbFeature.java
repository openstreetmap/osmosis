// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.database;

import org.openstreetmap.osmosis.core.store.GenericObjectReader;
import org.openstreetmap.osmosis.core.store.GenericObjectWriter;
import org.openstreetmap.osmosis.core.store.StoreClassRegister;
import org.openstreetmap.osmosis.core.store.StoreReader;
import org.openstreetmap.osmosis.core.store.StoreWriter;
import org.openstreetmap.osmosis.core.store.Storeable;


/**
 * A data class for representing a database record for an entity feature. This
 * aggregates a standard entity feature type with a field relating it to the
 * owning entity.
 * 
 * @author Brett Henderson
 * @param <T>
 *            The feature type to be encapsulated.
 */
public class DbFeature<T extends Storeable> implements Storeable {
	
	private long entityId;
	private T feature;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param entityId
	 *            The owning entity id.
	 * @param feature
	 *            The feature being referenced.
	 */
	public DbFeature(long entityId, T feature) {
		this.entityId = entityId;
		this.feature = feature;
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
	public DbFeature(StoreReader sr, StoreClassRegister scr) {
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
		new GenericObjectWriter(sw, scr).writeObject(feature);
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
	public T getFeature() {
		return feature;
	}
}
