// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.pgsimple.common;


/**
 * Represents the minimal geo-spatial information associated with a node.
 * 
 * @author Brett Henderson
 */
public class NodeLocation {
	private boolean valid;
	private double longitude;
	private double latitude;
	
	
	/**
	 * Creates a new empty instance which is marked as invalid.
	 */
	public NodeLocation() {
		this.valid = false;
	}
	
	
	/**
	 * Creates a new instance with populated location details.
	 * 
	 * @param longitude
	 *            The longitude of the node.
	 * @param latitude
	 *            The latitude of the node.
	 */
	public NodeLocation(double longitude, double latitude) {
		this.valid = true;
		this.longitude = longitude;
		this.latitude = latitude;
	}
	
	
	/**
	 * Indicates if the node is valid. A node may be invalid if it does not
	 * exist.
	 * 
	 * @return The valid flag.
	 */
	public boolean isValid() {
		return valid;
	}
	
	
	/**
	 * Gets the longitude of the node.
	 * 
	 * @return The node longitude.
	 */
	public double getLongitude() {
		return longitude;
	}
	
	
	/**
	 * Gets the latitude of the node.
	 * 
	 * @return The node latitude.
	 */
	public double getLatitude() {
		return latitude;
	}
}
