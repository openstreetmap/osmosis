package com.bretth.osm.conduit.xml;

import java.util.Map;

import com.bretth.osm.conduit.pipeline.OsmSourceManager;
import com.bretth.osm.conduit.pipeline.TaskManager;
import com.bretth.osm.conduit.pipeline.TaskManagerFactory;


public class XmlReaderFactory extends TaskManagerFactory {
	private static final String TASK_TYPE = "read-osm";
	
	
	protected TaskManager createTaskManagerImpl(Map<String, String> taskArgs, Map<String, String> pipeArgs) {
		return new OsmSourceManager(TASK_TYPE, new XmlReader(), pipeArgs);
	}
	
	
	protected String getTaskType() {
		return TASK_TYPE;
	}
}
