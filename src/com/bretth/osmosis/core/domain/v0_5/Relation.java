package com.bretth.osmosis.core.domain.v0_5;

import java.util.Date;

import com.bretth.osmosis.core.domain.common.Entity;
import com.bretth.osmosis.core.domain.common.EntityType;


/**
 * A data class representing a single OSM way.
 * 
 * @author Brett Henderson
 */
public class Relation extends Entity implements Comparable<Relation> {
	private static final long serialVersionUID = 1L;
	
	
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
	public Relation(long id, Date timestamp, String user) {
		super(id, timestamp, user);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public EntityType getType() {
		// FIXME: Must correct the type returned here.
		return EntityType.Way;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object o) {
		if (o instanceof Relation) {
			return compareTo((Relation) o) == 0;
		} else {
			return false;
		}
	}


	/**
	 * Compares this relation to the specified relation. The relation comparison
	 * is based on a comparison of id, timestamp, and tags in that order.
	 * 
	 * @param comparisonRelation
	 *            The relation to compare to.
	 * @return 0 if equal, <0 if considered "smaller", and >0 if considered
	 *         "bigger".
	 */
	public int compareTo(Relation comparisonRelation) {
		if (this.getId() < comparisonRelation.getId()) {
			return -1;
		}
		if (this.getId() > comparisonRelation.getId()) {
			return 1;
		}
		
		if (this.getTimestamp() == null && comparisonRelation.getTimestamp() != null) {
			return -1;
		}
		if (this.getTimestamp() != null && comparisonRelation.getTimestamp() == null) {
			return 1;
		}
		if (this.getTimestamp() != null && comparisonRelation.getTimestamp() != null) {
			int result;
			
			result = this.getTimestamp().compareTo(comparisonRelation.getTimestamp());
			
			if (result != 0) {
				return result;
			}
		}
		
		return compareTags(comparisonRelation.getTagList());
	}
}
