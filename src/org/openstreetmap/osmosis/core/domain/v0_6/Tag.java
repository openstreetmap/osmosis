// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.domain.v0_6;

import org.openstreetmap.osmosis.core.store.StoreClassRegister;
import org.openstreetmap.osmosis.core.store.StoreReader;
import org.openstreetmap.osmosis.core.store.StoreWriter;
import org.openstreetmap.osmosis.core.store.Storeable;


/**
 * A data class representing a single OSM tag.
 * 
 * @author Brett Henderson
 */
public class Tag implements Comparable<Tag>, Storeable {

    /**
     * The key identifying the tag.
     */
	private String key;
	/**
	 * The value associated with the tag.
	 */
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
	 * Creates a new instance.
	 * 
	 * @param sr
	 *            The store to read state from.
	 * @param scr
	 *            Maintains the mapping between classes and their identifiers
	 *            within the store.
	 */
	public Tag(StoreReader sr, StoreClassRegister scr) {
		this(sr.readString(), sr.readString());
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void store(StoreWriter sw, StoreClassRegister scr) {
		sw.writeString(key);
		sw.writeString(value);
	}
	
	
	/**
	 * Compares this tag to the specified tag. The tag comparison is based on
	 * a comparison of key and value in that order.
	 * 
	 * @param tag
	 *            The tag to compare to.
	 * @return 0 if equal, < 0 if considered "smaller", and > 0 if considered
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
  
    /** 
     * ${@inheritDoc}.
     */
    @Override
    public String toString() {
        return "Tag('" + getKey() + "'='" + getValue() + "')";
    }

}
