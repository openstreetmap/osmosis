package com.bretth.osmosis.core.cli;

import java.util.Collections;
import java.util.Map;


/**
 * Contains all command line information relating to a single task.
 * 
 * @author Brett Henderson
 */
public class TaskInfo {
	private String id;
	private String type;
	private Map<String, String> pipeArgs;
	private Map<String, String> configArgs;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param id The identifier of the task.
	 * @param type The type of the task.
	 * @param pipeArgs The pipe arguments for the task.
	 * @param configArgs The configuration arguments for the task.
	 */
	public TaskInfo(String id, String type, Map<String, String> pipeArgs, Map<String, String> configArgs) {
		this.id = id;
		this.type = type;
		this.pipeArgs = pipeArgs;
		this.configArgs = configArgs;
	}
	
	
	/**
	 * The unique identifier for the task.
	 * 
	 * @return The id.
	 */
	public String getId() {
		return id;
	}
	
	
	/**
	 * The type of the task to be created.
	 * 
	 * @return The type.
	 */
	public String getType() {
		return type;
	}
	
	
	/**
	 * The pipeline connection arguments for the task.
	 * 
	 * @return The pipeArgs.
	 */
	public Map<String, String> getPipeArgs() {
		return Collections.unmodifiableMap(pipeArgs);
	}
	
	
	/**
	 * The configuration arguments for the task.
	 * 
	 * @return The configArgs.
	 */
	public Map<String, String> getConfigArgs() {
		return Collections.unmodifiableMap(configArgs);
	}
}
