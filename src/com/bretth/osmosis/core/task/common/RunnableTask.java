// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package com.bretth.osmosis.core.task.common;



/**
 * Extends the basic Task interface with the Runnable capability.
 * 
 * @author Brett Henderson
 */
public interface RunnableTask extends Task, Runnable {
	// This interface combines Task and Runnable but doesn't introduce
	// methods of its own.
}
