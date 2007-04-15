package com.bretth.osm.conduit.mysql;

import java.util.Map;

import com.bretth.osm.conduit.pipeline.OsmSourceManager;
import com.bretth.osm.conduit.pipeline.TaskManager;
import com.bretth.osm.conduit.pipeline.TaskManagerFactory;


public class DatabaseReaderFactory extends TaskManagerFactory {
	private static final String TASK_TYPE = "read-mysql";
	
	
	protected TaskManager createTaskManagerImpl(Map<String, String> taskArgs, Map<String, String> pipeArgs) {
		return new OsmSourceManager(TASK_TYPE, new DatabaseReader(), pipeArgs);
	}
	
	
	protected String getTaskType() {
		return TASK_TYPE;
	}
}
