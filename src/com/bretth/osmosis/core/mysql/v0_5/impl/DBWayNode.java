package com.bretth.osmosis.core.mysql.v0_5.impl;

import com.bretth.osmosis.core.domain.v0_5.WayNode;


/**
 * A data class for representing a way node database record. This extends a way
 * node with fields relating it to the owning way.
 * 
 * @author Brett Henderson
 */
public class DBWayNode extends WayNode {
	private static final long serialVersionUID = 1L;
	
	
	private long wayId;
	private int sequenceId;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param wayId
	 *            The owning way id.
	 * @param nodeId
	 *            The node being referenced.
	 * @param sequenceId
	 *            The order of this node within the way.
	 */
	public DBWayNode(long wayId, long nodeId, int sequenceId) {
		super(nodeId);
		
		this.wayId = wayId;
		this.sequenceId = sequenceId;
	}
	
	
	/**
	 * @return The way id.
	 */
	public long getWayId() {
		return wayId;
	}
	
	
	/**
	 * @return The sequence id.
	 */
	public int getSequenceId() {
		return sequenceId;
	}
}
