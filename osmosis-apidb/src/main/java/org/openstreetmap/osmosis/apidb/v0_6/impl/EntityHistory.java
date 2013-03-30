// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.apidb.v0_6.impl;

import org.openstreetmap.osmosis.core.store.GenericObjectReader;
import org.openstreetmap.osmosis.core.store.GenericObjectWriter;
import org.openstreetmap.osmosis.core.store.StoreClassRegister;
import org.openstreetmap.osmosis.core.store.StoreReader;
import org.openstreetmap.osmosis.core.store.StoreWriter;
import org.openstreetmap.osmosis.core.store.Storeable;


/**
 * A data class representing a history record.
 * 
 * @author Brett Henderson
 * @param <T>
 *            The type of entity that this class stores history for.
 */
public class EntityHistory<T extends Storeable> implements Storeable {
	
	private T entity;
	private boolean visible;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param entity
	 *            The contained entity.
	 * @param visible
	 *            The visible field.
	 */
	public EntityHistory(T entity, boolean visible) {
		this.entity = entity;
		this.visible = visible;
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
	public EntityHistory(StoreReader sr, StoreClassRegister scr) {
		entity = (T) new GenericObjectReader(sr, scr).readObject();
		
		visible = sr.readBoolean();
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void store(StoreWriter sw, StoreClassRegister scr) {
		new GenericObjectWriter(sw, scr).writeObject(entity);
		sw.writeBoolean(visible);
	}
	
	
	/**
	 * Gets the contained entity.
	 * 
	 * @return The entity.
	 */
	public T getEntity() {
		return entity;
	}
	
	
	/**
	 * Gets the visible flag.
	 * 
	 * @return The visible flag.
	 */
	public boolean isVisible() {
		return visible;
	}
}
