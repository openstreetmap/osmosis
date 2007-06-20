package com.bretth.osm.conduit.change.impl;

import com.bretth.osm.conduit.data.Element;
import com.bretth.osm.conduit.data.Way;
import com.bretth.osm.conduit.task.ChangeAction;
import com.bretth.osm.conduit.task.ChangeSink;
import com.bretth.osm.conduit.task.Sink;


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
