package com.bretth.osmosis.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;


/**
 * A data class representing a single OSM way.
 * 
 * @author Brett Henderson
 */
public class Way extends Element implements Comparable<Way> {
	private static final long serialVersionUID = 1L;
	
	
	private Date timestamp;
	private List<SegmentReference> segmentReferenceList;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param id
	 *            The unique identifier.
	 * @param timestamp
	 *            The last updated timestamp.
	 */
	public Way(long id, Date timestamp) {
		super(id);
		
		this.timestamp = timestamp;
		
		segmentReferenceList = new ArrayList<SegmentReference>();
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public ElementType getElementType() {
		return ElementType.Way;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object o) {
		if (o instanceof Way) {
			return compareTo((Way) o) == 0;
		} else {
			return false;
		}
	}
	
	
	/**
	 * Compares this tag list to the specified tag list. The tag comparison is
	 * based on a comparison of key and value in that order.
	 * 
	 * @param comparisonSegmentReferenceList
	 *            The segment reference list to compare to.
	 * @return 0 if equal, <0 if considered "smaller", and >0 if considered
	 *         "bigger".
	 */
	protected int compareSegmentReferences(List<SegmentReference> comparisonSegmentReferenceList) {
		List<SegmentReference> list1;
		List<SegmentReference> list2;
		
		list1 = new ArrayList<SegmentReference>(segmentReferenceList);
		list2 = new ArrayList<SegmentReference>(comparisonSegmentReferenceList);
		
		Collections.sort(list1);
		Collections.sort(list2);
		
		// The list with the most elements is considered bigger.
		if (list1.size() != list2.size()) {
			return list1.size() - list2.size();
		}
		
		// Check the individual segment references.
		for (int i = 0; i < list1.size(); i++) {
			int result = list1.get(i).compareTo(list2.get(i));
			
			if (result != 0) {
				return result;
			}
		}
		
		// There are no differences.
		return 0;
	}


	/**
	 * Compares this way to the specified way. The way comparison is based on a
	 * comparison of id, timestamp, segmentReferenceList and tags in that order.
	 * 
	 * @param comparisonWay
	 *            The way to compare to.
	 * @return 0 if equal, <0 if considered "smaller", and >0 if considered
	 *         "bigger".
	 */
	public int compareTo(Way comparisonWay) {
		int segmentReferenceListResult;
		
		if (this.getId() < comparisonWay.getId()) {
			return -1;
		}
		if (this.getId() > comparisonWay.getId()) {
			return 1;
		}
		
		if (this.timestamp == null && comparisonWay.timestamp != null) {
			return -1;
		}
		if (this.timestamp != null && comparisonWay.timestamp == null) {
			return 1;
		}
		if (this.timestamp != null && comparisonWay.timestamp != null) {
			int result;
			
			result = this.timestamp.compareTo(comparisonWay.timestamp);
			
			if (result != 0) {
				return result;
			}
		}
		
		segmentReferenceListResult = compareSegmentReferences(
			comparisonWay.getSegmentReferenceList()
		);
		
		if (segmentReferenceListResult != 0) {
			return segmentReferenceListResult;
		}
		
		return compareTags(comparisonWay.getTagList());
	}
	
	
	/**
	 * @return The timestamp. 
	 */
	public Date getTimestamp() {
		return timestamp;
	}
	
	
	/**
	 * Returns the attached list of segment references. The returned list is
	 * read-only.
	 * 
	 * @return The segmentReferenceList.
	 */
	public List<SegmentReference> getSegmentReferenceList() {
		return Collections.unmodifiableList(segmentReferenceList);
	}
	
	
	/**
	 * Adds a new segment reference.
	 * 
	 * @param segmentReference
	 *            The segment reference to add.
	 */
	public void addSegmentReference(SegmentReference segmentReference) {
		segmentReferenceList.add(segmentReference);
	}
}
