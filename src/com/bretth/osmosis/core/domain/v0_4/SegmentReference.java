package com.bretth.osmosis.core.domain.v0_4;

import com.bretth.osmosis.core.store.StoreClassRegister;
import com.bretth.osmosis.core.store.StoreReader;
import com.bretth.osmosis.core.store.StoreWriter;
import com.bretth.osmosis.core.store.Storeable;


/**
 * A data class representing a reference to an OSM segment.
 * 
 * @author Brett Henderson
 */
public class SegmentReference implements Comparable<SegmentReference>, Storeable {
	
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
	 * Creates a new instance.
	 * 
	 * @param sr
	 *            The store to read state from.
	 * @param scr
	 *            Maintains the mapping between classes and their identifiers
	 *            within the store.
	 */
	public SegmentReference(StoreReader sr, StoreClassRegister scr) {
		this(sr.readLong());
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void store(StoreWriter sw, StoreClassRegister scr) {
		sw.writeLong(segmentId);
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
