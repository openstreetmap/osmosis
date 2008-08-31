// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package com.bretth.osmosis.core.mysql.v0_6.impl;

import com.bretth.osmosis.core.domain.v0_6.WayNode;
import com.bretth.osmosis.core.store.StoreClassRegister;
import com.bretth.osmosis.core.store.StoreReader;
import com.bretth.osmosis.core.store.StoreWriter;


/**
 * A data class for representing a way node database record. This extends a way
 * node with fields relating it to the owning way.
 * 
 * @author Brett Henderson
 */
public class DBWayNode extends DBEntityFeature<WayNode> {
	
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
		super(wayId, wayNode);
		
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
		super(sr, scr);
		this.sequenceId = sr.readInteger();
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void store(StoreWriter sw, StoreClassRegister scr) {
		super.store(sw, scr);
		sw.writeInteger(sequenceId);
	}
	
	
	/**
	 * @return The sequence id.
	 */
	public int getSequenceId() {
		return sequenceId;
	}
}
