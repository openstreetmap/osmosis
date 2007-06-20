package com.bretth.osm.conduit.data;


/**
 * A data class representing a single OSM segment.
 * 
 * @author Brett Henderson
 */
public class Segment extends Element implements Comparable<Segment> {
	private static final long serialVersionUID = 1L;
	
	
	private long from;
	private long to;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param id
	 *            The unique identifier.
	 * @param from
	 *            The id of the node marking the beginning of the segment.
	 * @param to
	 *            The id of the node marking the end of the segment.
	 */
	public Segment(long id, long from, long to) {
		super(id);
		
		this.from = from;
		this.to = to;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public ElementType getElementType() {
		return ElementType.Segment;
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
