// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.task.v0_6;


/**
 * Defines the interface for combining change sink and source functionality.
 * This is used by classes converting a change stream into an entity stream.
 * 
 * @author Brett Henderson
 */
public interface ChangeSinkSource extends ChangeSink, Source {
	// Interface only combines functionality of its extended interfaces.
}
