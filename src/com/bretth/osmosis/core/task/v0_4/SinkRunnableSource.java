package com.bretth.osmosis.core.task.v0_4;



/**
 * Defines the interface for combining Sink and RunnableSource functionality.
 * This is primarily intended for buffering tasks splitting processing across
 * multiple threads.
 * 
 * @author Brett Henderson
 */
public interface SinkRunnableSource extends Sink, RunnableSource {
	// This interface combines Sink and RunnableSource but doesn't introduce
	// methods of its own.
}
