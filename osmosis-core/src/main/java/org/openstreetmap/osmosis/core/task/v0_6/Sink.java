// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.task.v0_6;

import org.openstreetmap.osmosis.core.container.v0_6.EntityContainer;
import org.openstreetmap.osmosis.core.task.common.Task;


/**
 * Defines the interface for tasks consuming OSM data types.
 * 
 * @author Brett Henderson
 */
public interface Sink extends Task, Initializable {

	/**
	 * Process the entity.
	 * 
	 * @param entityContainer
	 *            The entity to be processed.
	 */
	void process(EntityContainer entityContainer);
}
