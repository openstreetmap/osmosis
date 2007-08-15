package com.bretth.osmosis.core.data;

import java.util.Date;


/**
 * A data class representing a single OSM node.
 * 
 * @author Brett Henderson
 */
public class Node extends Entity implements Comparable<Node> {
	private static final long serialVersionUID = 1L;
	
	
	private double latitude;
	private double longitude;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param id
	 *            The unique identifier.
	 * @param timestamp
	 *            The last updated timestamp.
	 * @param latitude
	 *            The geographic latitude.
	 * @param longitude
	 *            The geographic longitude.
	 */
	public Node(long id, Date timestamp, double latitude, double longitude) {
		super(id, timestamp);
		
		this.latitude = latitude;
		this.longitude = longitude;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public EntityType getType() {
		return EntityType.Node;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object o) {
		if (o instanceof Node) {
			return compareTo((Node) o) == 0;
		} else {
			return false;
		}
	}


	/**
	 * Compares this node to the specified node. The node comparison is based on
	 * a comparison of id, latitude, longitude, timestamp and tags in that
	 * order.
	 * 
	 * @param comparisonNode
	 *            The node to compare to.
	 * @return 0 if equal, <0 if considered "smaller", and >0 if considered
	 *         "bigger".
	 */
	public int compareTo(Node comparisonNode) {
		if (this.getId() < comparisonNode.getId()) {
			return -1;
		}
		
		if (this.getId() > comparisonNode.getId()) {
			return 1;
		}
		
		if (this.latitude < comparisonNode.latitude) {
			return -1;
		}
		
		if (this.latitude > comparisonNode.latitude) {
			return 1;
		}
		
		if (this.longitude < comparisonNode.longitude) {
			return -1;
		}
		
		if (this.longitude > comparisonNode.longitude) {
			return 1;
		}
		
		if (this.getTimestamp() == null && comparisonNode.getTimestamp() != null) {
			return -1;
		}
		if (this.getTimestamp() != null && comparisonNode.getTimestamp() == null) {
			return 1;
		}
		if (this.getTimestamp() != null && comparisonNode.getTimestamp() != null) {
			int result;
			
			result = this.getTimestamp().compareTo(comparisonNode.getTimestamp());
			
			if (result != 0) {
				return result;
			}
		}
		
		return compareTags(comparisonNode.getTagList());
	}
	
	
	/**
	 * @return The latitude. 
	 */
	public double getLatitude() {
		return latitude;
	}
	
	
	/**
	 * @return The longitude. 
	 */
	public double getLongitude() {
		return longitude;
	}
}
