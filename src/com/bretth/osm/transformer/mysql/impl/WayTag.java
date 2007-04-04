package com.bretth.osm.transformer.mysql.impl;

import com.bretth.osm.transformer.data.Tag;


public class WayTag extends Tag {
	private long wayId;
	
	public WayTag(long wayId, Tag tag) {
		super(tag.getKey(), tag.getValue());
		
		this.wayId = wayId;
	}
	
	
	public WayTag(long wayId, String key, String value) {
		super(key, value);
		
		this.wayId = wayId;
	}
	
	
	public long getWayId() {
		return wayId;
	}
}
