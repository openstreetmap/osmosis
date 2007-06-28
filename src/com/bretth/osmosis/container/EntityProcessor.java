package com.bretth.osmosis.container;


/**
 * EntityContainer implementations call implementations of this class to
 * perform entity type specific processing.
 * 
 * @author Brett Henderson
 */
public interface EntityProcessor {
	
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
