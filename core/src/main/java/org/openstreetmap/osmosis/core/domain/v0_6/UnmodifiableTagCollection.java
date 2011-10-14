// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.domain.v0_6;

import java.util.Collections;
import java.util.Map;

import org.openstreetmap.osmosis.core.store.StoreClassRegister;
import org.openstreetmap.osmosis.core.store.StoreWriter;
import org.openstreetmap.osmosis.core.util.CollectionWrapper;


/**
 * Wraps a tag collection and prevents modifications from being made to it.
 * 
 * @author Brett Henderson
 */
public class UnmodifiableTagCollection extends CollectionWrapper<Tag> implements TagCollection {

	private TagCollection wrappedTags;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param wrappedTags
	 *            The tags to wrap.
	 */
	public UnmodifiableTagCollection(TagCollection wrappedTags) {
		super(Collections.unmodifiableCollection(wrappedTags));
		
		this.wrappedTags = wrappedTags;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void store(StoreWriter sw, StoreClassRegister scr) {
		wrappedTags.store(sw, scr);
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public Map<String, String> buildMap() {
		return wrappedTags.buildMap();
	}
}
