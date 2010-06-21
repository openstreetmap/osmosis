// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.container.v0_6;

import org.openstreetmap.osmosis.core.store.GenericObjectReader;
import org.openstreetmap.osmosis.core.store.GenericObjectWriter;
import org.openstreetmap.osmosis.core.store.StoreClassRegister;
import org.openstreetmap.osmosis.core.store.StoreReader;
import org.openstreetmap.osmosis.core.store.StoreWriter;
import org.openstreetmap.osmosis.core.store.Storeable;
import org.openstreetmap.osmosis.core.task.common.ChangeAction;


/**
 * Holds an EntityContainer and an associated action.
 * 
 * @author Brett Henderson
 */
public class ChangeContainer implements Storeable {
	
	private EntityContainer entityContainer;
	private ChangeAction action;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param entityContainer
	 *            The entity to store.
	 * @param action
	 *            The action to store.
	 */
	public ChangeContainer(EntityContainer entityContainer, ChangeAction action) {
		this.entityContainer = entityContainer;
		this.action = action;
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
	public ChangeContainer(StoreReader sr, StoreClassRegister scr) {
		entityContainer = (EntityContainer) new GenericObjectReader(sr, scr).readObject();
		action = ChangeAction.valueOf(sr.readString());
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void store(StoreWriter sw, StoreClassRegister scr) {
		new GenericObjectWriter(sw, scr).writeObject(entityContainer);
		sw.writeString(action.toString());
	}
	
	
	/**
	 * Returns the contained entity.
	 * 
	 * @return The entity.
	 */
	public EntityContainer getEntityContainer() {
		return entityContainer;
	}
	
	
	/**
	 * Returns the contained action.
	 * 
	 * @return The action.
	 */
	public ChangeAction getAction() {
		return action;
	}
}
