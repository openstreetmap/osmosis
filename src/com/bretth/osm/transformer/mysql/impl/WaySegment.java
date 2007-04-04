package com.bretth.osm.transformer.mysql.impl;

import com.bretth.osm.transformer.data.SegmentReference;


public class WaySegment extends SegmentReference {
	private long wayId;
	private int sequenceId;
	
	
	public WaySegment(long wayId, SegmentReference segmentReference, int sequenceId) {
		super(segmentReference.getSegmentId());
		
		this.wayId = wayId;
		this.sequenceId = sequenceId;
	}
	
	
	public WaySegment(long wayId, long segmentId, int sequenceId) {
		super(segmentId);
		
		this.wayId = wayId;
		this.sequenceId = sequenceId;
	}
	
	
	public long getWayId() {
		return wayId;
	}
	
	
	public int getSequenceId() {
		return sequenceId;
	}
}
