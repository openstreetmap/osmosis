// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.domain.v0_6;

import org.openstreetmap.osmosis.core.store.StoreClassRegister;
import org.openstreetmap.osmosis.core.store.StoreReader;
import org.openstreetmap.osmosis.core.store.StoreWriter;
import org.openstreetmap.osmosis.core.store.Storeable;


/**
 * A data class representing a reference to an OSM node.
 * 
 * @author Brett Henderson
 */
public class WayNode implements Comparable<WayNode>, Storeable {
	
	private long nodeId;
	private double latitude;
	private double longitude;
	
	/**
	 * Creates a new instance.
	 * 
	 * @param nodeId
	 *            The unique identifier of the node being referred to.
	 */
	public WayNode(long nodeId) {
		this.nodeId = nodeId;
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param nodeId
	 *            The unique identifier of the node being referred to.
	 * @param latitude
	 *            The node's latitude
	 * @param longitude
	 *            The node's longitude
	 */
	public WayNode(long nodeId, double latitude, double longitude) {
		this(nodeId);
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
	public WayNode(StoreReader sr, StoreClassRegister scr) {
		this(sr.readLong(), sr.readDouble(), sr.readDouble());
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void store(StoreWriter sw, StoreClassRegister scr) {
		sw.writeLong(nodeId);
		sw.writeDouble(latitude);
		sw.writeDouble(longitude);
	}
	
	
	/**
	 * Compares this way node to the specified way node. The way node comparison
	 * is based on a comparison of nodeId.
	 * 
	 * @param wayNode
	 *            The way node to compare to.
	 * @return 0 if equal, &lt; 0 if considered "smaller", and &gt; 0 if
	 *         considered "bigger".
	 */
	public int compareTo(WayNode wayNode) {
		long result;
		
		result = this.nodeId - wayNode.nodeId;
		
		if (result > 0) {
			return 1;
		} else if (result < 0) {
			return -1;
		} else {
			return 0;
		}
	}
	
	/**
	 * @return The latitude (if the PBF file was built with osmium command 'add-locations-to-ways')
	 */
	public double getLatitude() {
		return latitude;
	}

	/**
	 * @return The longitude (if the PBF file was built with osmium command 'add-locations-to-ways')
	 */
	public double getLongitude() {
		return longitude;
	}

	/**
	 * @return The nodeId.
	 */
	public long getNodeId() {
		return nodeId;
	}

    /** 
     * ${@inheritDoc}.
     */
    @Override
    public String toString() {
        return "WayNode(nodeID=" + getNodeId() + ")";
    }
}
