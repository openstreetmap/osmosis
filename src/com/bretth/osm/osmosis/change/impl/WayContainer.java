package com.bretth.osm.osmosis.change.impl;

import com.bretth.osm.osmosis.data.Element;
import com.bretth.osm.osmosis.data.Way;
import com.bretth.osm.osmosis.task.ChangeAction;
import com.bretth.osm.osmosis.task.ChangeSink;
import com.bretth.osm.osmosis.task.Sink;


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
	public void process(Sink sink) {
		sink.processWay(way);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void processChange(ChangeSink changeSink, ChangeAction action) {
		changeSink.processWay(way, action);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Element getElement() {
		return way;
	}
}
