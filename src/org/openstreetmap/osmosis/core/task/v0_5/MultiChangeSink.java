// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package org.openstreetmap.osmosis.core.task.v0_5;


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
	public ChangeSink getChangeSink(int instance);


	/**
	 * Returns the number of change sinks provided by this task.
	 * 
	 * @return The number of change sinks.
	 */
	public int getChangeSinkCount();
}
