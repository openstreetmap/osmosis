package com.bretth.osm.conduit.mysql;

import java.util.Map;

import com.bretth.osm.conduit.pipeline.OsmSinkManager;


public class DatabaseWriterBuilder extends OsmSinkManager {
	private static final String TASK_NAME = "write-mysql";
	
	
	public DatabaseWriter createTask(Map<String, String> args) {
		return new DatabaseWriter();
	}
	
	
	protected String getTaskName() {
		return TASK_NAME;
	}
}
