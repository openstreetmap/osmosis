package com.bretth.osm.conduit.xml;

import java.util.Map;

import com.bretth.osm.conduit.pipeline.OsmSinkManager;


public class XmlWriterBuilder extends OsmSinkManager {
	private static final String TASK_NAME = "write-osm";
	
	
	public XmlWriter createTask(Map<String, String> args) {
		return new XmlWriter();
	}
	
	
	protected String getTaskName() {
		return TASK_NAME;
	}
}
