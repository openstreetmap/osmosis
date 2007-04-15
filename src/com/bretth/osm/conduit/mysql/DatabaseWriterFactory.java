package com.bretth.osm.conduit.mysql;

import java.util.Map;

import com.bretth.osm.conduit.pipeline.OsmSinkManager;
import com.bretth.osm.conduit.pipeline.TaskManager;
import com.bretth.osm.conduit.pipeline.TaskManagerFactory;


public class DatabaseWriterFactory extends TaskManagerFactory {
	private static final String TASK_TYPE = "write-mysql";
	
	
	protected TaskManager createTaskManagerImpl(Map<String, String> taskArgs, Map<String, String> pipeArgs) {
		return new OsmSinkManager(TASK_TYPE, new DatabaseWriter(), pipeArgs);
	}
	
	
	protected String getTaskType() {
		return TASK_TYPE;
	}
}
