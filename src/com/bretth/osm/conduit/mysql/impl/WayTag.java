package com.bretth.osm.conduit.mysql.impl;

import com.bretth.osm.conduit.data.Tag;


/**
 * A data class for representing a way tag database record. This extends a
 * tag with fields relating it to the owning way.
 * 
 * @author Brett Henderson
 */
public class WayTag extends Tag {
	private static final long serialVersionUID = 1L;
	
	
	private long wayId;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param wayId
	 *            The owning way id.
	 * @param key
	 *            The tag key.
	 * @param value
	 *            The tag value.
	 */
	public WayTag(long wayId, String key, String value) {
		super(key, value);
		
		this.wayId = wayId;
	}
	
	
	/**
	 * @return The way id.
	 */
	public long getWayId() {
		return wayId;
	}
}
