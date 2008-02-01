// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package com.bretth.osmosis.core.task.v0_5;

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
