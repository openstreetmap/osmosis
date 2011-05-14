// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.domain.v0_6;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.openstreetmap.osmosis.core.store.StoreClassRegister;
import org.openstreetmap.osmosis.core.store.StoreReader;
import org.openstreetmap.osmosis.core.store.StoreWriter;
import org.openstreetmap.osmosis.core.util.CollectionWrapper;
import org.openstreetmap.osmosis.core.util.IntAsChar;


/**
 * Maintains a collection of tags.
 * 
 * @author Brett Henderson
 */
public class MetaTagCollectionImpl extends CollectionWrapper<MetaTag> implements MetaTagCollection {
	
	/**
	 * Creates a new instance.
	 */
	public MetaTagCollectionImpl() {
		super(new ArrayList<MetaTag>());
	}
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param tags
	 *            The initial tags.
	 */
	public MetaTagCollectionImpl(Collection<? extends MetaTag> tags) {
		super(new ArrayList<MetaTag>(tags));
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
	public MetaTagCollectionImpl(StoreReader sr, StoreClassRegister scr) {
		super(new ArrayList<MetaTag>());
		
		int tagCount;
		
		tagCount = sr.readCharacter();
		for (int i = 0; i < tagCount; i++) {
			add(new MetaTag(sr, scr));
		}
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void store(StoreWriter sw, StoreClassRegister scr) {
		sw.writeCharacter(IntAsChar.intToChar(size()));
		for (MetaTag tag : this) {
			tag.store(sw, scr);
		}
	}


	/**
	 * {@inheritDoc}
	 */
	public Map<String, Object> buildMap() {
		Map<String, Object> tagMap;
		
		tagMap = new HashMap<String, Object>(size());
		for (MetaTag tag : this) {
			tagMap.put(tag.getKey(), tag.getValue());
		}
		
		return tagMap;
	}
}
