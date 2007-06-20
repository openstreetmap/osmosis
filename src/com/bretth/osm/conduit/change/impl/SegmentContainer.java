package com.bretth.osm.conduit.change.impl;

import com.bretth.osm.conduit.data.Element;
import com.bretth.osm.conduit.data.Segment;
import com.bretth.osm.conduit.task.ChangeAction;
import com.bretth.osm.conduit.task.ChangeSink;
import com.bretth.osm.conduit.task.Sink;


/**
 * Element container implementation for segments.
 * 
 * @author Brett Henderson
 */
public class SegmentContainer extends ElementContainer {
	private Segment segment;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param segment
	 *            The segment to wrap.
	 */
	public SegmentContainer(Segment segment) {
		this.segment = segment;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void process(Sink sink) {
		sink.processSegment(segment);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void processChange(ChangeSink changeSink, ChangeAction action) {
		changeSink.processSegment(segment, action);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Element getElement() {
		return segment;
	}
}
