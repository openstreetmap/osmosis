package com.bretth.osm.conduit.pipeline;

import java.util.Map;

import com.bretth.osm.conduit.task.OsmTransformer;
import com.bretth.osm.conduit.task.Task;


/**
 * A task manager implementation for OsmTransformer task implementations.
 * 
 * @author Brett Henderson
 */
public class OsmTransformerManager extends OsmSinkManager {
	private OsmTransformer task;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param taskId
	 *            A unique identifier for the task. This is used to produce
	 *            meaningful errors when errors occur.
	 * @param task
	 *            The task instance to be managed.
	 * @param pipeArgs
	 *            The arguments defining input and output pipes for the task,
	 *            pipes are a logical concept for identifying how the tasks are
	 *            connected together.
	 */
	public OsmTransformerManager(String taskId, OsmTransformer task, Map<String, String> pipeArgs) {
		super(taskId, task, pipeArgs);
		
		this.task = task;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void connect(Map<String, Task> pipeTasks) {
		// Connect the pipeline inputs.
		super.connect(pipeTasks);
		
		// Connect the pipeline outputs.
		OsmRunnableSourceManager.connectImpl(task, getTaskId(), pipeTasks, getPipeArgs());
	}
}
