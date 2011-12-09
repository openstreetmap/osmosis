// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.misc.v0_6;

import java.util.Collections;

import org.openstreetmap.osmosis.core.task.v0_6.RunnableSource;
import org.openstreetmap.osmosis.core.task.v0_6.Sink;


/**
 * An OSM data source that produces an empty entity stream.
 * 
 * @author Brett Henderson
 */
public class EmptyReader implements RunnableSource {
	private Sink sink;


	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setSink(Sink sink) {
		this.sink = sink;
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public void run() {
		try {
			sink.initialize(Collections.<String, Object>emptyMap());
			sink.complete();
		} finally {
			sink.release();
		}
	}
}
