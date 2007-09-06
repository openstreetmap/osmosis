package com.bretth.osmosis.core.domain.v0_4;

import java.util.Date;

import com.bretth.osmosis.core.domain.common.Entity;
import com.bretth.osmosis.core.domain.common.EntityType;


/**
 * A data class representing a single OSM segment.
 * 
 * @author Brett Henderson
 */
public class Segment extends Entity implements Comparable<Segment> {
	private static final long serialVersionUID = 1L;
	
	
	private long from;
	private long to;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param id
	 *            The unique identifier.
	 * @param timestamp
	 *            The last updated timestamp.
	 * @param user
	 *            The name of the user that last modified this entity.
	 * @param from
	 *            The id of the node marking the beginning of the segment.
	 * @param to
	 *            The id of the node marking the end of the segment.
	 */
	public Segment(long id, Date timestamp, String user, long from, long to) {
		super(id, timestamp, user);
		
		this.from = from;
		this.to = to;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public EntityType getType() {
		return EntityType.Segment;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object o) {
		if (o instanceof Segment) {
			return compareTo((Segment) o) == 0;
		} else {
			return false;
		}
	}


	/**
	 * Compares this segment to the specified segment. The way comparison is
	 * based on a comparison of id, from, to and tags in that order.
	 * 
	 * @param comparisonSegment
	 *            The segment to compare to.
	 * @return 0 if equal, <0 if considered "smaller", and >0 if considered
	 *         "bigger".
	 */
	public int compareTo(Segment comparisonSegment) {
		if (this.getId() < comparisonSegment.getId()) {
			return -1;
		}
		if (this.getId() > comparisonSegment.getId()) {
			return 1;
		}
		
		if (this.from < comparisonSegment.from) {
			return -1;
		}
		if (this.from > comparisonSegment.from) {
			return 1;
		}
		
		if (this.to < comparisonSegment.to) {
			return -1;
		}
		if (this.to > comparisonSegment.to) {
			return 1;
		}
		
		if (this.getTimestamp() == null && comparisonSegment.getTimestamp() != null) {
			return -1;
		}
		if (this.getTimestamp() != null && comparisonSegment.getTimestamp() == null) {
			return 1;
		}
		if (this.getTimestamp() != null && comparisonSegment.getTimestamp() != null) {
			int result;
			
			result = this.getTimestamp().compareTo(comparisonSegment.getTimestamp());
			
			if (result != 0) {
				return result;
			}
		}
		
		return compareTags(comparisonSegment.getTagList());
	}
	
	
	/**
	 * @return The from. 
	 */
	public long getFrom() {
		return from;
	}
	
	
	/**
	 * @return The to. 
	 */
	public long getTo() {
		return to;
	}
}
