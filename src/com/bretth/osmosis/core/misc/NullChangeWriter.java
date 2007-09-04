package com.bretth.osmosis.core.misc;

import com.bretth.osmosis.core.container.v0_4.ChangeContainer;
import com.bretth.osmosis.core.task.v0_4.ChangeSink;


/**
 * An OSM change sink that discards all data sent to it. This is primarily
 * intended for benchmarking purposes.
 * 
 * @author Brett Henderson
 */
public class NullChangeWriter implements ChangeSink {
	
	/**
	 * {@inheritDoc}
	 */
	public void process(ChangeContainer change) {
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
