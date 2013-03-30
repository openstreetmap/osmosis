// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.set.v0_6.impl;

import java.util.Map;

import org.openstreetmap.osmosis.core.container.v0_6.EntityContainer;
import org.openstreetmap.osmosis.core.store.DataPostbox;
import org.openstreetmap.osmosis.core.task.v0_6.Sink;


/**
 * A sink that writes all of its data to a postbox to be read by another thread.
 * 
 * @author Brett Henderson
 */
public class DataPostboxSink implements Sink {
	private DataPostbox<EntityContainer> postbox;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param postbox
	 *            The postbox to write all incoming data into.
	 */
	public DataPostboxSink(DataPostbox<EntityContainer> postbox) {
		this.postbox = postbox;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
    public void initialize(Map<String, Object> metaData) {
		postbox.initialize(metaData);
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public void process(EntityContainer entity) {
		postbox.put(entity);
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public void complete() {
		postbox.complete();
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public void release() {
		postbox.release();
	}
}
