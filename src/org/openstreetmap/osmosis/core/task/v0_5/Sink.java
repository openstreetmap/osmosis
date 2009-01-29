// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package org.openstreetmap.osmosis.core.task.v0_5;

import org.openstreetmap.osmosis.core.container.v0_5.EntityContainer;
import org.openstreetmap.osmosis.core.lifecycle.Completable;
import org.openstreetmap.osmosis.core.task.common.Task;


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
	void process(EntityContainer entityContainer);
}
