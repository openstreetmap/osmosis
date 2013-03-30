// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.container.v0_6;

import org.openstreetmap.osmosis.core.domain.v0_6.Bound;
import org.openstreetmap.osmosis.core.store.StoreClassRegister;
import org.openstreetmap.osmosis.core.store.StoreReader;
import org.openstreetmap.osmosis.core.store.StoreWriter;


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
	public BoundContainer getWriteableInstance() {
		if (bound.isReadOnly()) {
			return new BoundContainer(bound.getWriteableInstance());
		} else {
			return this;
		}
	}
}
