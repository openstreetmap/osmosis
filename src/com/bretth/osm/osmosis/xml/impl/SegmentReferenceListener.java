package com.bretth.osm.osmosis.xml.impl;

import com.bretth.osm.osmosis.data.SegmentReference;


/**
 * Provides the definition of a class receiving segment references.
 * 
 * @author Brett Henderson
 */
public interface SegmentReferenceListener {
	/**
	 * Processes the segment reference.
	 * 
	 * @param segmentReference
	 *            The segment reference.
	 */
	void processSegmentReference(SegmentReference segmentReference);
}
