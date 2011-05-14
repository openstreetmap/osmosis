// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.domain.v0_6;

import org.openstreetmap.osmosis.core.store.StoreClassRegister;
import org.openstreetmap.osmosis.core.store.StoreReader;
import org.openstreetmap.osmosis.core.store.StoreWriter;
import org.openstreetmap.osmosis.core.store.Storeable;


/**
 * A data class representing a meta tag that can be attached to an entity. This
 * differs from a standard tag in that it is not part of the standard OSM data
 * model and provides a way of attaching additional data for passing through the
 * Osmosis pipeline.
 * 
 * @author Brett Henderson
 */
public class MetaTag implements Comparable<MetaTag>, Storeable {

    /**
     * The key identifying the tag.
     */
	private String key;
	/**
	 * The value associated with the tag.
	 */
	private Object value;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param key
	 *            The key identifying the tag.
	 * @param value
	 *            The value associated with the tag.
	 */
	public MetaTag(String key, Object value) {
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
	public MetaTag(StoreReader sr, StoreClassRegister scr) {
		this(sr.readString(), sr.readString());
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void store(StoreWriter sw, StoreClassRegister scr) {
		sw.writeString(key);
		sw.writeString(value.toString());
	}
	
	
	/**
	 * Compares this tag to the specified tag. The tag comparison is based on
	 * a comparison of the key only.
	 * 
	 * @param tag
	 *            The tag to compare to.
	 * @return 0 if equal, < 0 if considered "smaller", and > 0 if considered
	 *         "bigger".
	 */
	public int compareTo(MetaTag tag) {
		return key.compareTo(tag.key);
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
	public Object getValue() {
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
