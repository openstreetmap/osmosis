// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.task.v0_6;

import org.openstreetmap.osmosis.core.container.v0_6.ChangeContainer;
import org.openstreetmap.osmosis.core.task.common.Task;


/**
 * Defines the interface for all tasks consuming OSM changes to data.
 * 
 * @author Brett Henderson
 */
public interface ChangeSink extends Task, Initializable {

	/**
	 * Process the change.
	 * 
	 * @param change
	 *            The change to be processed.
	 */
	void process(ChangeContainer change);
}
