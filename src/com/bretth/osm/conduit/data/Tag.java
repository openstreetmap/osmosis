package com.bretth.osm.conduit.data;


/**
 * A data class representing a single OSM tag.
 * 
 * @author Brett Henderson
 */
public class Tag {
	private String key;
	private String value;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param key
	 *            The key identifying the tag.
	 * @param value
	 *            The value associated with the tag.
	 */
	public Tag(String key, String value) {
		this.key = key;
		this.value = value;
	}
	
	
	/**
	 * @return The key.
	 */
	public String getKey() {
		return key;
	}
	
	
	/**
	 * @return The value.
	 */
	public String getValue() {
		return value;
	}
}
