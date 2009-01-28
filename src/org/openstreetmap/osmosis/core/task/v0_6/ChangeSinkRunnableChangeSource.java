// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package org.openstreetmap.osmosis.core.task.v0_6;


/**
 * Defines the interface for combining ChangeSink and RunnableChangeSource functionality.
 * This is primarily intended for buffering tasks splitting processing across
 * multiple threads.
 * 
 * @author Brett Henderson
 */
public interface ChangeSinkRunnableChangeSource extends ChangeSink, RunnableChangeSource {
	// This interface combines ChangeSink and RunnableChangeSource but doesn't introduce
	// methods of its own.
}
