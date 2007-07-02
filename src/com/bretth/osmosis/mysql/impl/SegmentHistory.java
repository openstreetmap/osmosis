package com.bretth.osmosis.mysql.impl;

import com.bretth.osmosis.data.Segment;


/**
 * A data class representing a segment history record.
 * 
 * @author Brett Henderson
 */
public class SegmentHistory {
	
	private Segment segment;
	private boolean visible;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param segment
	 *            The contained node.
	 * @param visible
	 *            The visible field.
	 */
	public SegmentHistory(Segment segment, boolean visible) {
		this.segment = segment;
		this.visible = visible;
	}
	
	
	/**
	 * Gets the contained segment.
	 * 
	 * @return The segment.
	 */
	public Segment getSegment() {
		return segment;
	}
	
	
	/**
	 * Gets the visible flag.
	 * 
	 * @return The visible flag.
	 */
	public boolean isVisible() {
		return visible;
	}
}
