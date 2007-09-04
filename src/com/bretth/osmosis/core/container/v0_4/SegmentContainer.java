package com.bretth.osmosis.core.container.v0_4;

import com.bretth.osmosis.core.domain.v0_4.Segment;


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
