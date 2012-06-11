// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.container.v0_6;

import org.openstreetmap.osmosis.core.domain.v0_6.Way;
import org.openstreetmap.osmosis.core.store.StoreClassRegister;
import org.openstreetmap.osmosis.core.store.StoreReader;
import org.openstreetmap.osmosis.core.store.StoreWriter;


/**
 * Entity container implementation for ways.
 * 
 * @author Brett Henderson
 */
public class WayContainer extends EntityContainer {
	
	private Way way;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param way
	 *            The way to wrap.
	 */
	public WayContainer(Way way) {
		this.way = way;
	}
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param sr
	 *            The store to read state from.
	 * @param scr
	 *            Maintains the mapping between classes and their identifiers
	 *            within the store.
	 */
	public WayContainer(StoreReader sr, StoreClassRegister scr) {
		way = new Way(sr, scr);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void store(StoreWriter sw, StoreClassRegister scr) {
		way.store(sw, scr);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void process(EntityProcessor processor) {
		processor.process(this);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Way getEntity() {
		return way;
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public WayContainer getWriteableInstance() {
		if (way.isReadOnly()) {
			return new WayContainer(way.getWriteableInstance());
		} else {
			return this;
		}
	}
}
