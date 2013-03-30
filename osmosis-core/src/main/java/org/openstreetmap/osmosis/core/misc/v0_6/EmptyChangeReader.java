// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.misc.v0_6;

import java.util.Collections;

import org.openstreetmap.osmosis.core.task.v0_6.ChangeSink;
import org.openstreetmap.osmosis.core.task.v0_6.RunnableChangeSource;


/**
 * An OSM data source that produces an empty change stream.
 * 
 * @author Brett Henderson
 */
public class EmptyChangeReader implements RunnableChangeSource {
	private ChangeSink changeSink;


	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setChangeSink(ChangeSink changeSink) {
		this.changeSink = changeSink;
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public void run() {
		try {
			changeSink.initialize(Collections.<String, Object>emptyMap());
			changeSink.complete();
		} finally {
			changeSink.release();
		}
	}
}
