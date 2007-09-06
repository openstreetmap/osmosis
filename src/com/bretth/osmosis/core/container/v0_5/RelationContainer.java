package com.bretth.osmosis.core.container.v0_5;

import com.bretth.osmosis.core.domain.v0_5.Relation;


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
