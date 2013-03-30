// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.misc.v0_6;

import java.util.Map;

import org.openstreetmap.osmosis.core.container.v0_6.EntityContainer;
import org.openstreetmap.osmosis.core.task.v0_6.Sink;


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
	@Override
	public void initialize(Map<String, Object> metaTags) {
		// Nothing to do.
	}


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
