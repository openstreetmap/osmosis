// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package com.bretth.osmosis.core.task.v0_5;

import com.bretth.osmosis.core.container.v0_5.EntityContainer;
import com.bretth.osmosis.core.task.common.Task;


/**
 * Defines the interface for tasks consuming OSM data types.
 * 
 * @author Brett Henderson
 */
public interface Sink extends Task {
	
	/**
	 * Process the entity.
	 * 
	 * @param entityContainer
	 *            The entity to be processed.
	 */
	public void process(EntityContainer entityContainer);
	
	/**
	 * Performs finalisation tasks such as database commits as necessary to
	 * complete the task. Must be called by clients when all objects have been
	 * processed. It should not be called in exception scenarios. Chained
	 * implementations will call their output sinks.
	 */
	public void complete();
	
	/**
	 * Performs resource cleanup tasks such as closing files, or database
	 * connections. This must be called after all processing is complete. It
	 * should be called within a finally block to ensure it is called in
	 * exception scenarios. Chained implementations will call their output
	 * sinks.
	 */
	public void release();
}
