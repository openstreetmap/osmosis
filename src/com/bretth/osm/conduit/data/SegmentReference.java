package com.bretth.osm.conduit.data;


/**
 * A data class representing a reference to an OSM segment.
 * 
 * @author Brett Henderson
 */
public class SegmentReference implements Comparable<SegmentReference> {
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
	 * Compares this segment reference to the specified segment reference. The
	 * segment reference comparison is based on a comparison of segmentId.
	 * 
	 * @param segmentReference
	 *            The segment reference to compare to.
	 * @return 0 if equal, <0 if considered "smaller", and >0 if considered
	 *         "bigger".
	 */
	public int compareTo(SegmentReference segmentReference) {
		long result;
		
		result = this.segmentId - segmentReference.segmentId;
		
		if (result > 0) {
			return 1;
		} else if (result < 0) {
			return -1;
		} else {
			return 0;
		}
	}
	
	
	/**
	 * @return The segmentId.
	 */
	public long getSegmentId() {
		return segmentId;
	}
}
