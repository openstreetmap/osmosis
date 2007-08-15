package com.bretth.osmosis.core.task;


/**
 * Extends the basic Task interface with the Runnable capability.
 * 
 * @author Brett Henderson
 */
public interface RunnableTask extends Task, Runnable {
	// This interface combines Task and Runnable but doesn't introduce
	// methods of its own.
}
