package com.bretth.osmosis.core.xml.v0_4.impl;

import com.bretth.osmosis.core.domain.v0_4.Tag;


/**
 * Provides the definition of a class receiving tags.
 * 
 * @author Brett Henderson
 */
public interface TagListener {
	
	/**
	 * Processes the tag.
	 * 
	 * @param tag
	 *            The tag.
	 */
	void processTag(Tag tag);
}
