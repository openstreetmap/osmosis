// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package org.openstreetmap.osmosis.core.task.v0_6;

import org.openstreetmap.osmosis.core.task.common.Task;


/**
 * Defines the interface for all tasks producing OSM changes to data.
 * 
 * @author Brett Henderson
 */
public interface ChangeSource extends Task {
	
	/**
	 * Sets the change sink to send data to.
	 * 
	 * @param changeSink
	 *            The sink for receiving all produced data.
	 */
	void setChangeSink(ChangeSink changeSink);
}
