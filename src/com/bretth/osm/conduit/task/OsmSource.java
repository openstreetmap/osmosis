package com.bretth.osm.conduit.task;


public interface OsmSource extends Task {
	void setOsmSink(OsmSink osmSink);
}
