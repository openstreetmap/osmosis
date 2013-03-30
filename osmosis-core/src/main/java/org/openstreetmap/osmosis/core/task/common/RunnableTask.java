// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.task.common;



/**
 * Extends the basic Task interface with the Runnable capability.
 * 
 * @author Brett Henderson
 */
public interface RunnableTask extends Task, Runnable {
	// This interface combines Task and Runnable but doesn't introduce
	// methods of its own.
}
