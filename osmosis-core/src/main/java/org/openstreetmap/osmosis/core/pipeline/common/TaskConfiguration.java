// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.pipeline.common;

import java.util.Collections;
import java.util.Map;


/**
 * Contains all information relating to the configuration of a single task.
 * 
 * @author Brett Henderson
 */
public class TaskConfiguration {
	private String id;
	private String type;
	private Map<String, String> pipeArgs;
	private Map<String, String> configArgs;
	private String defaultArg;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param id The identifier of the task.
	 * @param type The type of the task.
	 * @param pipeArgs The pipe arguments for the task.
	 * @param configArgs The configuration arguments for the task.
	 * @param defaultArg The default argument for the task.
	 */
	public TaskConfiguration(
			String id, String type, Map<String, String> pipeArgs, Map<String, String> configArgs, String defaultArg) {
		this.id = id;
		this.type = type;
		this.pipeArgs = Collections.unmodifiableMap(pipeArgs);
		this.configArgs = Collections.unmodifiableMap(configArgs);
		this.defaultArg = defaultArg;
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
		return pipeArgs;
	}
	
	
	/**
	 * The configuration arguments for the task.
	 * 
	 * @return The configArgs.
	 */
	public Map<String, String> getConfigArgs() {
		return configArgs;
	}
	
	
	/**
	 * Contains the single default argument (if supplied) to the task.
	 * 
	 * @return The defaultArg or null if not available.
	 */
	public String getDefaultArg() {
		return defaultArg;
	}
}
