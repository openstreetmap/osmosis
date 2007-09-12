package com.bretth.osmosis.core.mysql.v0_5.impl;

import com.bretth.osmosis.core.domain.v0_5.Tag;


/**
 * A data class for representing an entity tag database record. This extends a
 * tag with fields relating it to the owning entity.
 * 
 * @author Brett Henderson
 */
public class DBEntityTag extends Tag {
	private static final long serialVersionUID = 1L;
	
	
	private long entityId;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param entityId
	 *            The owning entity id.
	 * @param key
	 *            The tag key.
	 * @param value
	 *            The tag value.
	 */
	public DBEntityTag(long entityId, String key, String value) {
		super(key, value);
		
		this.entityId = entityId;
	}
	
	
	/**
	 * @return The entity id.
	 */
	public long getEntityId() {
		return entityId;
	}
}
