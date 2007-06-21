package com.bretth.osmosis.change.impl;

import com.bretth.osmosis.data.Element;
import com.bretth.osmosis.data.Segment;
import com.bretth.osmosis.task.ChangeAction;
import com.bretth.osmosis.task.ChangeSink;
import com.bretth.osmosis.task.Sink;


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
