// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package com.bretth.osmosis.core.container.v0_5;

import com.bretth.osmosis.core.domain.v0_5.Way;
import com.bretth.osmosis.core.store.StoreClassRegister;
import com.bretth.osmosis.core.store.StoreReader;
import com.bretth.osmosis.core.store.StoreWriter;


/**
 * Entity container implementation for ways.
 * 
 * @author Brett Henderson
 */
public class WayContainer extends EntityContainer {
	private static final long serialVersionUID = 1L;
	
	
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
}
