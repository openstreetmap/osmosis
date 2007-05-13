package com.bretth.osm.conduit.mysql.impl;

import com.bretth.osm.conduit.data.SegmentReference;


/**
 * A data class for representing a way segment database record. This extends a
 * segment reference with fields relating it to the owning way.
 * 
 * @author Brett Henderson
 */
public class WaySegment extends SegmentReference {
	private long wayId;
	private int sequenceId;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param wayId
	 *            The owning way id.
	 * @param segmentId
	 *            The segment being referenced.
	 * @param sequenceId
	 *            The order of this segment within the way.
	 */
	public WaySegment(long wayId, long segmentId, int sequenceId) {
		super(segmentId);
		
		this.wayId = wayId;
		this.sequenceId = sequenceId;
	}
	
	
	/**
	 * @return The way id.
	 */
	public long getWayId() {
		return wayId;
	}
	
	
	/**
	 * @return The sequence id.
	 */
	public int getSequenceId() {
		return sequenceId;
	}
}
