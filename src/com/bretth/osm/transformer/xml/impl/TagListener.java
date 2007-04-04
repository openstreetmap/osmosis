package com.bretth.osm.transformer.xml.impl;

import com.bretth.osm.transformer.data.Tag;


public interface TagListener {
	void processTag(Tag tag);
}
