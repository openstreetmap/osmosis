package com.bretth.osmosis.container;

import com.bretth.osmosis.data.Segment;


/**
 * Entity container implementation for segments.
 * 
 * @author Brett Henderson
 */
public class SegmentContainer extends EntityContainer {
	private static final long serialVersionUID = 1L;
	
	
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
	public void process(EntityProcessor processor) {
		processor.process(this);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Segment getEntity() {
		return segment;
	}
}
