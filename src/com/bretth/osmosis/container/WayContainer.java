package com.bretth.osmosis.container;

import com.bretth.osmosis.data.Way;


/**
 * Element container implementation for ways.
 * 
 * @author Brett Henderson
 */
public class WayContainer extends ElementContainer {
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
	public void process(ElementProcessor processor) {
		processor.process(this);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Way getElement() {
		return way;
	}
}
