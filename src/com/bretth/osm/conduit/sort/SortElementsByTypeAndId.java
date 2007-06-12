package com.bretth.osm.conduit.sort;

import com.bretth.osm.conduit.data.Node;
import com.bretth.osm.conduit.data.Segment;
import com.bretth.osm.conduit.data.Way;
import com.bretth.osm.conduit.task.Sink;
import com.bretth.osm.conduit.task.SinkSource;

/**
 * A data stream filter that sorts all of the elements first by type and then by
 * their identifier. This will result in an output stream that lists nodes,
 * followed by segments followed by ways all sorted by their identifiers.
 * 
 * @author Brett Henderson
 */
public class SortElementsByTypeAndId implements SinkSource {
	private Sink sink;
	
	
	/**
	 * {@inheritDoc}
	 */
	public void processNode(Node node) {
		// TODO Auto-generated method stub
		
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void processSegment(Segment segment) {
		// TODO Auto-generated method stub
		
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void processWay(Way way) {
		// TODO Auto-generated method stub
		
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void setSink(Sink sink) {
		this.sink = sink;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void complete() {
		sink.complete();
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void release() {
		sink.release();
	}
}
