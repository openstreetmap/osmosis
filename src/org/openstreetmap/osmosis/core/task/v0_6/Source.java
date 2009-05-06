// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.task.v0_6;

import org.openstreetmap.osmosis.core.task.common.Task;


/**
 * Defines the interface for tasks producing OSM data types.
 * 
 * @author Brett Henderson
 */
public interface Source extends Task {
	
	/**
	 * Sets the osm sink to send data to.
	 * 
	 * @param sink
	 *            The sink for receiving all produced data.
	 */
	void setSink(Sink sink);
}
