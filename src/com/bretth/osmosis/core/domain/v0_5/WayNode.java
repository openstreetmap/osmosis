package com.bretth.osmosis.core.domain.v0_5;

import java.io.Serializable;


/**
 * A data class representing a reference to an OSM node.
 * 
 * @author Brett Henderson
 */
public class WayNode implements Comparable<WayNode>, Serializable {
	private static final long serialVersionUID = 1L;
	
	
	private long nodeId;
	
	
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
	 * Compares this way node to the specified way node. The way node comparison
	 * is based on a comparison of nodeId.
	 * 
	 * @param wayNode
	 *            The way node to compare to.
	 * @return 0 if equal, <0 if considered "smaller", and >0 if considered
	 *         "bigger".
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
	 * @return The nodeId.
	 */
	public long getNodeId() {
		return nodeId;
	}
}
