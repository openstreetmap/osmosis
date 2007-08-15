package com.bretth.osmosis.core.xml.impl;

import com.bretth.osmosis.core.data.SegmentReference;


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
