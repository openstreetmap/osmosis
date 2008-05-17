// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package com.bretth.osmosis.core.xml.v0_6.impl;

import com.bretth.osmosis.core.domain.v0_6.Tag;


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
