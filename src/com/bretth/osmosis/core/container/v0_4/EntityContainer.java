package com.bretth.osmosis.core.container.v0_4;

import com.bretth.osmosis.core.domain.v0_4.Entity;
import com.bretth.osmosis.core.store.Storeable;


/**
 * Implementations of this class allow data entities to be processed without the
 * caller knowing their type.
 * 
 * @author Brett Henderson
 */
public abstract class EntityContainer implements Storeable {
	/**
	 * Calls the appropriate process method with the contained entity.
	 * 
	 * @param processor
	 *            The processor to invoke.
	 */
	public abstract void process(EntityProcessor processor);
	
	
	/**
	 * Returns the contained entity.
	 * 
	 * @return The entity.
	 */
	public abstract Entity getEntity();
}
