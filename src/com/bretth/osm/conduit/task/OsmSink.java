package com.bretth.osm.conduit.task;

import com.bretth.osm.conduit.data.Node;
import com.bretth.osm.conduit.data.Segment;
import com.bretth.osm.conduit.data.Way;


public interface OsmSink extends Task {
	public void addNode(Node node);
	
	public void addSegment(Segment segment);
	
	public void addWay(Way way);
	
	public void complete();
	
	public void release();
}
