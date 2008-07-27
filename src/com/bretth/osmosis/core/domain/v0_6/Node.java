// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package com.bretth.osmosis.core.domain.v0_6;

import java.util.Date;

import com.bretth.osmosis.core.domain.common.SimpleTimestampContainer;
import com.bretth.osmosis.core.domain.common.TimestampContainer;
import com.bretth.osmosis.core.store.StoreClassRegister;
import com.bretth.osmosis.core.store.StoreReader;
import com.bretth.osmosis.core.store.StoreWriter;
import com.bretth.osmosis.core.util.FixedPrecisionCoordinateConvertor;


/**
 * A data class representing a single OSM node.
 * 
 * @author Brett Henderson
 */
public class Node extends Entity implements Comparable<Node> {
	
	private double latitude;
	private double longitude;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param id
	 *            The unique identifier.
	 * @param timestamp
	 *            The last updated timestamp.
	 * @param user
	 *            The name of the user that last modified this entity.
	 * @param userId
	 *            The userId associated with the user name.
	 * @param version
	 *            The version of the entity.
	 * @param latitude
	 *            The geographic latitude.
	 * @param longitude
	 *            The geographic longitude.
	 */
	public Node(long id, Date timestamp, String user, int userId, int version, double latitude, double longitude) {
		// Chain to the more-specific constructor
		this(id, new SimpleTimestampContainer(timestamp), user, userId, version, latitude, longitude);
	}
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param id
	 *            The unique identifier.
	 * @param timestampContainer
	 *            The container holding the timestamp in an alternative
	 *            timestamp representation.
	 * @param user
	 *            The name of the user that last modified this entity.
	 * @param userId
	 *            The userId associated with the user name.
	 * @param version
	 *            The version of the entity.
	 * @param latitude
	 *            The geographic latitude.
	 * @param longitude
	 *            The geographic longitude.
	 */
	public Node(long id, TimestampContainer timestampContainer, String user, int userId, int version, double latitude, double longitude) {
		super(id, timestampContainer, user, userId, version);
		
		this.latitude = latitude;
		this.longitude = longitude;
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
	public Node(StoreReader sr, StoreClassRegister scr) {
		super(sr, scr);
		
		this.latitude = FixedPrecisionCoordinateConvertor.convertToDouble(sr.readInteger());
		this.longitude = FixedPrecisionCoordinateConvertor.convertToDouble(sr.readInteger());
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void store(StoreWriter sw, StoreClassRegister scr) {
		super.store(sw, scr);
		
		sw.writeInteger(FixedPrecisionCoordinateConvertor.convertToFixed(latitude));
		sw.writeInteger(FixedPrecisionCoordinateConvertor.convertToFixed(longitude));
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
	 * a comparison of id, version, latitude, longitude, timestamp and tags in that
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
		
		if (this.getVersion() < comparisonNode.getVersion()) {
			return -1;
		}
		
		if (this.getVersion() > comparisonNode.getVersion()) {
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
