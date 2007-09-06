package com.bretth.osmosis.core.domain.v0_5;

import java.io.Serializable;


/**
 * A data class representing a single OSM tag.
 * 
 * @author Brett Henderson
 */
public class Tag implements Comparable<Tag>, Serializable {
	private static final long serialVersionUID = 1L;
	
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
	 * Compares this tag to the specified tag. The tag comparison is based on
	 * a comparison of key and value in that order.
	 * 
	 * @param tag
	 *            The tag to compare to.
	 * @return 0 if equal, <0 if considered "smaller", and >0 if considered
	 *         "bigger".
	 */
	public int compareTo(Tag tag) {
		int keyResult;
		
		keyResult = this.key.compareTo(tag.key);
		
		if (keyResult != 0) {
			return keyResult;
		}
		
		return this.value.compareTo(tag.value);
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
