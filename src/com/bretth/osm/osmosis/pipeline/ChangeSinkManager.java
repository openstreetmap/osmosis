package com.bretth.osm.osmosis.pipeline;

import java.util.Map;

import com.bretth.osm.osmosis.task.ChangeSink;
import com.bretth.osm.osmosis.task.ChangeSource;


/**
 * A task manager implementation for ChangeSink task implementations.
 * 
 * @author Brett Henderson
 */
public class ChangeSinkManager extends TaskManager {
	private ChangeSink task;
	
	
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
	public ChangeSinkManager(String taskId, ChangeSink task, Map<String, String> pipeArgs) {
		super(taskId, pipeArgs);
		
		this.task = task;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void connect(PipeTasks pipeTasks) {
		ChangeSource source;
		
		// Get the input task. A sink only has one input, this corresponds to
		// pipe index 0.
		source = (ChangeSource) getInputTask(pipeTasks, 0, ChangeSource.class);
		
		// Cast the input feed to the correct type.
		// Connect the tasks.
		source.setChangeSink(task);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void run() {
		// Nothing to do for a sink because it passively receives data.
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void waitForCompletion() {
		// Nothing to do for a sink because it passively receives data.
	}
}
