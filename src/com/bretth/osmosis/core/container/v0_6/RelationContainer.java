// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package com.bretth.osmosis.core.container.v0_6;

import com.bretth.osmosis.core.domain.v0_6.Relation;
import com.bretth.osmosis.core.store.StoreClassRegister;
import com.bretth.osmosis.core.store.StoreReader;
import com.bretth.osmosis.core.store.StoreWriter;


/**
 * Entity container implementation for relations.
 * 
 * @author Brett Henderson
 */
public class RelationContainer extends EntityContainer {
	private static final long serialVersionUID = 1L;
	
	
	private Relation relation;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param relation
	 *            The relation to wrap.
	 */
	public RelationContainer(Relation relation) {
		this.relation = relation;
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
	public RelationContainer(StoreReader sr, StoreClassRegister scr) {
		relation = new Relation(sr, scr);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void store(StoreWriter sw, StoreClassRegister scr) {
		relation.store(sw, scr);
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
	public Relation getEntity() {
		return relation;
	}
}
