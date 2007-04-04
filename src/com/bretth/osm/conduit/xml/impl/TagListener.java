package com.bretth.osm.conduit.xml.impl;

import com.bretth.osm.conduit.data.Tag;


public interface TagListener {
	void processTag(Tag tag);
}
