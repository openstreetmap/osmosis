// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package com.bretth.osmosis.core.container.v0_5;

import com.bretth.osmosis.core.store.GenericObjectReader;
import com.bretth.osmosis.core.store.GenericObjectWriter;
import com.bretth.osmosis.core.store.StoreClassRegister;
import com.bretth.osmosis.core.store.StoreReader;
import com.bretth.osmosis.core.store.StoreWriter;
import com.bretth.osmosis.core.store.Storeable;
import com.bretth.osmosis.core.task.common.ChangeAction;


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
