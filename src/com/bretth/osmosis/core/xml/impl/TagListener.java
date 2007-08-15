package com.bretth.osmosis.core.xml.impl;

import com.bretth.osmosis.core.data.Tag;


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
