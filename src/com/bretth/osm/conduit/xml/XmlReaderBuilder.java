package com.bretth.osm.conduit.xml;

import java.util.Map;

import com.bretth.osm.conduit.pipeline.OsmSourceManager;


public class XmlReaderBuilder extends OsmSourceManager {
	private static final String TASK_NAME = "read-osm";
	
	
	public XmlReader createTask(Map<String, String> args) {
		return new XmlReader();
	}
	
	
	protected String getTaskName() {
		return TASK_NAME;
	}
}
