// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package com.bretth.osmosis.core.task.v0_6;

import com.bretth.osmosis.core.container.v0_6.EntityContainer;
import com.bretth.osmosis.core.lifecycle.Completable;
import com.bretth.osmosis.core.task.common.Task;


/**
 * Defines the interface for tasks consuming OSM data types.
 * 
 * @author Brett Henderson
 */
public interface Sink extends Task, Completable {
	
	/**
	 * Process the entity.
	 * 
	 * @param entityContainer
	 *            The entity to be processed.
	 */
	public void process(EntityContainer entityContainer);
}
