package com.bretth.osm.conduit.data;


/**
 * A data class representing a reference to an OSM segment.
 * 
 * @author Brett Henderson
 */
public class SegmentReference {
	private long segmentId;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param segmentId
	 *            The unique identifier of the segment being referred to.
	 */
	public SegmentReference(long segmentId) {
		this.segmentId = segmentId;
	}
	
	
	/**
	 * @return The segmentId.
	 */
	public long getSegmentId() {
		return segmentId;
	}
}
