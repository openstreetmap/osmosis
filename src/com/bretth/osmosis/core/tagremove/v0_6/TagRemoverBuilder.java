// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package com.bretth.osmosis.core.tagremove.v0_6;

import java.util.HashSet;
import java.util.Iterator;

import com.bretth.osmosis.core.container.v0_6.EntityContainerBuilder;
import com.bretth.osmosis.core.domain.v0_6.EntityBuilder;
import com.bretth.osmosis.core.domain.v0_6.Tag;


/**
 * Provides drop tags functionality utilising the builder mechanism.
 * 
 * @author Brett Henderson
 */
public class TagRemoverBuilder extends EntityContainerBuilder {
	private HashSet<String> keysToDrop;
	private String[] keyPrefixesToDrop;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param keyList
	 *            Comma separated list of keys of tags to be removed.
	 * @param keyPrefixList
	 *            Comma separated list of key prefixes of tags to be removed.
	 */
	public TagRemoverBuilder(String keyList, String keyPrefixList) {
		keysToDrop = new HashSet<String>();
		String[] keys = keyList.split(",");
		for (int i = 0; i < keys.length; i++) {
			keysToDrop.add(keys[i]);
		}
		keyPrefixesToDrop = keyPrefixList.split(",");
		if (keyPrefixesToDrop[0] == "") {
			keyPrefixesToDrop = new String[] {};
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected boolean processEntity(EntityBuilder<?> builder) {
		boolean modified;
		
		modified = false;
		
		for (Iterator<Tag> i = builder.getTags().iterator(); i.hasNext();) {
			Tag tag;
			
			tag = i.next();
			
			if (keysToDrop.contains(tag.getKey())) {
				i.remove();
				modified = true;
			} else {
				for (String prefix: keyPrefixesToDrop) {
					if (tag.getKey().startsWith(prefix)) {
						i.remove();
						modified = true;
					   	break;
					}
				}
			}
		}
		
		return modified;
	}
}
