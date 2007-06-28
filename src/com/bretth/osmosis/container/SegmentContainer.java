package com.bretth.osmosis.container;

import com.bretth.osmosis.data.Segment;


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
	public void process(ElementProcessor processor) {
		processor.process(this);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Segment getElement() {
		return segment;
	}
}
