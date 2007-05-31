package com.bretth.osm.conduit.misc;

import com.bretth.osm.conduit.data.Node;
import com.bretth.osm.conduit.data.Segment;
import com.bretth.osm.conduit.data.Way;
import com.bretth.osm.conduit.task.ChangeAction;
import com.bretth.osm.conduit.task.ChangeSink;


/**
 * An OSM change sink that discards all data sent to it. This is primarily
 * intended for benchmarking purposes.
 * 
 * @author Brett Henderson
 */
public class NullChangeWriter implements ChangeSink {
	
	/**
	 * {@inheritDoc}
	 */
	public void processNode(Node node, ChangeAction action) {
		// Discard the data.
	}


	/**
	 * {@inheritDoc}
	 */
	public void processSegment(Segment segment, ChangeAction action) {
		// Discard the data.
	}


	/**
	 * {@inheritDoc}
	 */
	public void processWay(Way way, ChangeAction action) {
		// Discard the data.
	}
	
	
	/**
	 * Flushes all changes to file.
	 */
	public void complete() {
		// Nothing to do.
	}
	
	
	/**
	 * Cleans up any open file handles.
	 */
	public void release() {
		// Nothing to do.
	}
}
