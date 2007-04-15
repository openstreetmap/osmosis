package com.bretth.osm.conduit.xml;

import java.util.Map;

import com.bretth.osm.conduit.pipeline.OsmSinkManager;
import com.bretth.osm.conduit.pipeline.TaskManager;
import com.bretth.osm.conduit.pipeline.TaskManagerFactory;


public class XmlWriterFactory extends TaskManagerFactory {
	private static final String TASK_TYPE = "write-osm";
	
	
	protected TaskManager createTaskManagerImpl(Map<String, String> taskArgs, Map<String, String> pipeArgs) {
		return new OsmSinkManager(TASK_TYPE, new XmlWriter(), pipeArgs);
	}
	
	
	protected String getTaskType() {
		return TASK_TYPE;
	}
}
