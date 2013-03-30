// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.task.v0_6;

import org.openstreetmap.osmosis.core.task.common.Task;


/**
 * Defines the interface for tasks producing multiple streams of OSM data.
 * 
 * @author Brett Henderson
 */
public interface MultiSource extends Task {
	
	/**
	 * Retrieves a specific source that can then have a sink attached.
	 * 
	 * @param index
	 * 			  The index of the source to retrieve.
	 * @return The requested index.
	 */
	Source getSource(int index);
	
	
	/**
	 * Indicates the number of sources that the task provides.
	 * 
	 * @return The number of sources.
	 */
	int getSourceCount();
}
