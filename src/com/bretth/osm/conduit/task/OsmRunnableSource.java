package com.bretth.osm.conduit.task;


public interface OsmRunnableSource extends OsmSource, Runnable {
	// This interface exists to create an OsmSource that requires a thread of
	// execution.
}
