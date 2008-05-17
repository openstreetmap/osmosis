// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package com.bretth.osmosis.core.container.v0_6;

import com.bretth.osmosis.core.domain.v0_6.Bound;
import com.bretth.osmosis.core.store.StoreClassRegister;
import com.bretth.osmosis.core.store.StoreReader;
import com.bretth.osmosis.core.store.StoreWriter;


/**
 * Entity container implementation for bound.
 * 
 * @author knewman
 */
public class BoundContainer extends EntityContainer {

	private Bound bound;


	/**
	 * Creates a new instance.
	 * 
	 * @param bound
	 *            The bound to wrap.
	 */
	public BoundContainer(Bound bound) {
		this.bound = bound;
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
	public BoundContainer(StoreReader sr, StoreClassRegister scr) {
		bound = new Bound(sr, scr);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Bound getEntity() {
		return bound;
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public void store(StoreWriter sw, StoreClassRegister scr) {
		bound.store(sw, scr);
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public void process(EntityProcessor processor) {
		processor.process(this);
	}
}
