package com.bretth.osmosis.xml.impl;

import com.bretth.osmosis.data.SegmentReference;


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
