package com.bretth.osmosis.core.mysql.v0_5.impl;

import com.bretth.osmosis.core.domain.v0_5.WayNode;
import com.bretth.osmosis.core.store.StoreClassRegister;
import com.bretth.osmosis.core.store.StoreReader;
import com.bretth.osmosis.core.store.StoreWriter;
import com.bretth.osmosis.core.store.Storeable;


/**
 * A data class for representing a way node database record. This extends a way
 * node with fields relating it to the owning way.
 * 
 * @author Brett Henderson
 */
public class DBWayNode implements Storeable {
	
	private long wayId;
	private WayNode wayNode;
	private int sequenceId;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param wayId
	 *            The owning way id.
	 * @param wayNode
	 *            The way node being referenced.
	 * @param sequenceId
	 *            The order of this node within the way.
	 */
	public DBWayNode(long wayId, WayNode wayNode, int sequenceId) {
		this.wayId = wayId;
		this.wayNode = wayNode;
		this.sequenceId = sequenceId;
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
	public DBWayNode(StoreReader sr, StoreClassRegister scr) {
		this(
			sr.readLong(),
			new WayNode(sr, scr),
			sr.readInteger()
		);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void store(StoreWriter sw, StoreClassRegister scr) {
		sw.writeLong(wayId);
		wayNode.store(sw, scr);
		sw.writeInteger(sequenceId);
	}
	
	
	/**
	 * @return The way id.
	 */
	public long getWayId() {
		return wayId;
	}
	
	
	/**
	 * @return The way node.
	 */
	public WayNode getWayNode() {
		return wayNode;
	}
	
	
	/**
	 * @return The sequence id.
	 */
	public int getSequenceId() {
		return sequenceId;
	}
}
