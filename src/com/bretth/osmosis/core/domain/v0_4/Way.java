package com.bretth.osmosis.core.domain.v0_4;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import com.bretth.osmosis.core.store.StoreClassRegister;
import com.bretth.osmosis.core.store.StoreReader;
import com.bretth.osmosis.core.store.StoreWriter;
import com.bretth.osmosis.core.store.Storeable;


/**
 * A data class representing a single OSM way.
 * 
 * @author Brett Henderson
 */
public class Way extends Entity implements Comparable<Way>, Storeable {
	
	private List<SegmentReference> segmentReferenceList;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param id
	 *            The unique identifier.
	 * @param timestamp
	 *            The last updated timestamp.
	 * @param user
	 *            The name of the user that last modified this entity.
	 */
	public Way(long id, Date timestamp, String user) {
		super(id, timestamp, user);
		
		segmentReferenceList = new ArrayList<SegmentReference>();
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
	public Way(StoreReader sr, StoreClassRegister scr) {
		super(sr, scr);
		
		int segmentCount;
		
		segmentCount = sr.readInteger();
		
		segmentReferenceList = new ArrayList<SegmentReference>();
		for (int i = 0; i < segmentCount; i++) {
			addSegmentReference(new SegmentReference(sr, scr));
		}
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void store(StoreWriter sw, StoreClassRegister scr) {
		super.store(sw, scr);
		
		sw.writeInteger(segmentReferenceList.size());
		for (SegmentReference segmentReference : segmentReferenceList) {
			segmentReference.store(sw, scr);
		}
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public EntityType getType() {
		return EntityType.Way;
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
	 * Compares this segment list to the specified segment list. The comparison
	 * is based on a direct comparison of the segment ids.
	 * 
	 * @param comparisonSegmentReferenceList
	 *            The segment reference list to compare to.
	 * @return 0 if equal, <0 if considered "smaller", and >0 if considered
	 *         "bigger".
	 */
	protected int compareSegmentReferences(List<SegmentReference> comparisonSegmentReferenceList) {
		// The list with the most entities is considered bigger.
		if (segmentReferenceList.size() != comparisonSegmentReferenceList.size()) {
			return segmentReferenceList.size() - comparisonSegmentReferenceList.size();
		}
		
		// Check the individual segment references.
		for (int i = 0; i < segmentReferenceList.size(); i++) {
			int result = segmentReferenceList.get(i).compareTo(comparisonSegmentReferenceList.get(i));
			
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
		
		if (this.getTimestamp() == null && comparisonWay.getTimestamp() != null) {
			return -1;
		}
		if (this.getTimestamp() != null && comparisonWay.getTimestamp() == null) {
			return 1;
		}
		if (this.getTimestamp() != null && comparisonWay.getTimestamp() != null) {
			int result;
			
			result = this.getTimestamp().compareTo(comparisonWay.getTimestamp());
			
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
	
	
	/**
	 * Adds all segment references in the collection to the node.
	 * 
	 * @param segmentReferences
	 *            The collection of segment references to be added.
	 */
	public void addSegmentReferences(Collection<SegmentReference> segmentReferences) {
		segmentReferenceList.addAll(segmentReferences);
	}
}
