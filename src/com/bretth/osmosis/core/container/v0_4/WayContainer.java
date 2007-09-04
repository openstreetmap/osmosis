package com.bretth.osmosis.core.container.v0_4;

import com.bretth.osmosis.core.domain.v0_4.Way;


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
