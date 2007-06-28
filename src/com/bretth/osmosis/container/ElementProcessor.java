package com.bretth.osmosis.container;


/**
 * ElementContainer implementations call implementations of this class to
 * perform element type specific processing.
 * 
 * @author Brett Henderson
 */
public interface ElementProcessor {
	
	/**
	 * Process the node.
	 * 
	 * @param node
	 *            The node to be processed.
	 */
	public void process(NodeContainer node);
	
	/**
	 * Process the segment.
	 * 
	 * @param segment
	 *            The segment to be processed.
	 */
	public void process(SegmentContainer segment);
	
	/**
	 * Process the way.
	 * 
	 * @param way
	 *            The way to be processed.
	 */
	public void process(WayContainer way);
}
