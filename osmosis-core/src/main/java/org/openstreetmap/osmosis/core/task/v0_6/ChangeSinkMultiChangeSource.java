// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.task.v0_6;

/**
 * Defines the interface for combining ChangeSink and MultiChangeSource
 * functionality. This is primarily intended for tasks splitting change data to
 * multiple destinations.
 * 
 * @author Brett Henderson
 */
public interface ChangeSinkMultiChangeSource extends ChangeSink, MultiChangeSource {
	// This interface combines ChangeSink and MultiChangeSource but doesn't
	// introduce
	// methods of its own.
}
