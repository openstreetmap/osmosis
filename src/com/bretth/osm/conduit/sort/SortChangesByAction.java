package com.bretth.osm.conduit.sort;

import com.bretth.osm.conduit.data.Node;
import com.bretth.osm.conduit.data.Segment;
import com.bretth.osm.conduit.data.Way;
import com.bretth.osm.conduit.task.ChangeAction;
import com.bretth.osm.conduit.task.ChangeSink;
import com.bretth.osm.conduit.task.ChangeSinkChangeSource;


/**
 * A change stream filter that re-orders the change stream so that all creates
 * are listed first, followed by modifies, followed by deletes.
 * 
 * @author Brett Henderson
 */
public class SortChangesByAction implements ChangeSinkChangeSource {
	
	private ChangeSink changeSink;
	
	
	/**
	 * {@inheritDoc}
	 */
	public void processNode(Node node, ChangeAction action) {
		// TODO: This shouldn't be a pass through.
		changeSink.processNode(node, action);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void processSegment(Segment segment, ChangeAction action) {
		// TODO: This shouldn't be a pass through.
		changeSink.processSegment(segment, action);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void processWay(Way way, ChangeAction action) {
		// TODO: This shouldn't be a pass through.
		changeSink.processWay(way, action);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void setChangeSink(ChangeSink changeSink) {
		this.changeSink = changeSink;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void complete() {
		changeSink.complete();
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void release() {
		changeSink.release();
	}
}
