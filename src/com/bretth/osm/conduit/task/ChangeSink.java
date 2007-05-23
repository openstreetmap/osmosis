package com.bretth.osm.conduit.task;

import com.bretth.osm.conduit.data.Node;
import com.bretth.osm.conduit.data.Segment;
import com.bretth.osm.conduit.data.Way;


/**
 * Defines the interface for all tasks consuming OSM changes to data.
 * 
 * @author Brett Henderson
 */
public interface ChangeSink extends Task {
	
	/**
	 * Process the node.
	 * 
	 * @param node
	 *            The node to be processed.
	 * @param action
	 *            The particular change action to be performed.
	 */
	public void processNode(Node node, ChangeAction action);
	
	/**
	 * Process the segment.
	 * 
	 * @param segment
	 *            The segment to be processed.
	 * @param action
	 *            The particular change action to be performed.
	 */
	public void processSegment(Segment segment, ChangeAction action);
	
	/**
	 * Process the way.
	 * 
	 * @param way
	 *            The way to be processed.
	 * @param action
	 *            The particular change action to be performed.
	 */
	public void processWay(Way way, ChangeAction action);
	
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
