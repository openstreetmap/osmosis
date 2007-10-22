package com.bretth.osmosis.core.mysql.v0_4.impl;

import com.bretth.osmosis.core.domain.v0_4.SegmentReference;
import com.bretth.osmosis.core.store.StoreClassRegister;
import com.bretth.osmosis.core.store.StoreReader;
import com.bretth.osmosis.core.store.StoreWriter;
import com.bretth.osmosis.core.store.Storeable;


/**
 * A data class for representing a way segment database record. This extends a
 * segment reference with fields relating it to the owning way.
 * 
 * @author Brett Henderson
 */
public class WaySegment implements Storeable {
	
	private long wayId;
	private SegmentReference segmentReference;
	private int sequenceId;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param wayId
	 *            The owning way id.
	 * @param segmentReference
	 *            The segment reference being referenced.
	 * @param sequenceId
	 *            The order of this segment within the way.
	 */
	public WaySegment(long wayId, SegmentReference segmentReference, int sequenceId) {
		this.wayId = wayId;
		this.segmentReference = segmentReference;
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
	public WaySegment(StoreReader sr, StoreClassRegister scr) {
		this(
			sr.readLong(),
			new SegmentReference(sr, scr),
			sr.readInteger()
		);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void store(StoreWriter sw, StoreClassRegister scr) {
		sw.writeLong(wayId);
		segmentReference.store(sw, scr);
		sw.writeInteger(sequenceId);
	}
	
	
	/**
	 * @return The way id.
	 */
	public long getWayId() {
		return wayId;
	}
	
	
	/**
	 * @return The sequence reference.
	 */
	public SegmentReference getSegmentReference() {
		return segmentReference;
	}
	
	
	/**
	 * @return The sequence id.
	 */
	public int getSequenceId() {
		return sequenceId;
	}
}
