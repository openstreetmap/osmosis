// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.task.v0_6;


/**
 * Defines the interface for tasks consuming multiple streams of data. It allows
 * a task to expose multiple change sinks. Classes may choose to combine this
 * with other multi sink style interfaces where data streams of different types
 * are to be consumed.
 * 
 * @author Brett Henderson
 */
public interface MultiChangeSink {
	/**
	 * Obtains one of the change sinks exposed by the task.
	 * 
	 * @param instance
	 *            The index of the change sink to be returned.
	 * @return The change sink.
	 */
	ChangeSink getChangeSink(int instance);


	/**
	 * Returns the number of change sinks provided by this task.
	 * 
	 * @return The number of change sinks.
	 */
	int getChangeSinkCount();
}
