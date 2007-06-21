package com.bretth.osmosis.task;

import com.bretth.osmosis.data.Node;
import com.bretth.osmosis.data.Segment;
import com.bretth.osmosis.data.Way;


/**
 * Defines the interface for tasks consuming OSM data types.
 * 
 * @author Brett Henderson
 */
public interface Sink extends Task {
	
	/**
	 * Process the node.
	 * 
	 * @param node
	 *            The node to be processed.
	 */
	public void processNode(Node node);
	
	/**
	 * Process the segment.
	 * 
	 * @param segment
	 *            The segment to be processed.
	 */
	public void processSegment(Segment segment);
	
	/**
	 * Process the way.
	 * 
	 * @param way
	 *            The way to be processed.
	 */
	public void processWay(Way way);
	
	/**
	 * Performs finalisation tasks such as database commits as necessary to
	 * complete the task. Must be called by clients when all objects have been
	 * processed. It should not be called in exception scenarios. Chained
	 * implementations will call their output sinks.
	 */
	public void complete();
	
	/**
	 * Performs resource cleanup tasks such as closing files, or database
	 * connections. This must be called after all processing is complete. It
	 * should be called within a finally block to ensure it is called in
	 * exception scenarios. Chained implementations will call their output
	 * sinks.
	 */
	public void release();
}
