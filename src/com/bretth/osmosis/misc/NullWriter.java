package com.bretth.osmosis.misc;

import com.bretth.osmosis.container.EntityContainer;
import com.bretth.osmosis.task.Sink;


/**
 * An OSM data sink that discards all data sent to it. This is primarily
 * intended for benchmarking purposes.
 * 
 * @author Brett Henderson
 */
public class NullWriter implements Sink {
	
	/**
	 * {@inheritDoc}
	 */
	public void process(EntityContainer entityContainer) {
		// Discard the data.
	}
	
	
	/**
	 * Flushes all changes to file.
	 */
	public void complete() {
		// Nothing to do.
	}
	
	
	/**
	 * Cleans up any open file handles.
	 */
	public void release() {
		// Nothing to do.
	}
}
