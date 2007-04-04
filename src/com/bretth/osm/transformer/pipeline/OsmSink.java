package com.bretth.osm.transformer.pipeline;

import com.bretth.osm.transformer.data.Node;
import com.bretth.osm.transformer.data.Segment;
import com.bretth.osm.transformer.data.Way;


public interface OsmSink {
	public void addNode(Node node);
	
	public void addSegment(Segment segment);
	
	public void addWay(Way way);
	
	public void complete();
	
	public void release();
}
