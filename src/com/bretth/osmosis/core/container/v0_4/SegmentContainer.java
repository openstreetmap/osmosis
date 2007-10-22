package com.bretth.osmosis.core.container.v0_4;

import com.bretth.osmosis.core.domain.v0_4.Segment;
import com.bretth.osmosis.core.store.StoreClassRegister;
import com.bretth.osmosis.core.store.StoreReader;
import com.bretth.osmosis.core.store.StoreWriter;


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
	 * Creates a new instance.
	 * 
	 * @param sr
	 *            The store to read state from.
	 * @param scr
	 *            Maintains the mapping between classes and their identifiers
	 *            within the store.
	 */
	public SegmentContainer(StoreReader sr, StoreClassRegister scr) {
		segment = new Segment(sr, scr);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void store(StoreWriter sw, StoreClassRegister scr) {
		segment.store(sw, scr);
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
