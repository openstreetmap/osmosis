// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.task.v0_6;

/**
 * Defines the interface for combining Sink and MultiSource functionality. This
 * is primarily intended for tasks splitting data to multiple destinations.
 * 
 * @author Brett Henderson
 */
public interface SinkMultiSource extends Sink, MultiSource {
	// This interface combines Sink and MultiSource but doesn't introduce
	// methods of its own.
}
