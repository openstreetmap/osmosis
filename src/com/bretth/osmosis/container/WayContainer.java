package com.bretth.osmosis.container;

import com.bretth.osmosis.data.Way;


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
