package com.bretth.osm.conduit.task;



public interface OsmSource extends Task, Runnable {
	void setOsmSink(OsmSink osmSink);
	
	void run();
}
