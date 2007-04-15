package com.bretth.osm.conduit.mysql;

import java.util.Map;

import com.bretth.osm.conduit.pipeline.OsmSourceManager;


public class DatabaseReaderManager extends OsmSourceManager {
	private static final String TASK_NAME = "read-mysql";
	
	
	public DatabaseReader createTask(Map<String, String> args) {
		return new DatabaseReader();
	}
	
	
	protected String getTaskName() {
		return TASK_NAME;
	}
}
