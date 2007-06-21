package com.bretth.osm.osmosis.pipeline;

import java.util.Map;

import com.bretth.osm.osmosis.task.SinkSource;
import com.bretth.osm.osmosis.task.Source;


/**
 * A task manager implementation for task performing sink and source
 * functionality.
 * 
 * @author Brett Henderson
 */
public class SinkSourceManager extends TaskManager {
	private SinkSource task;
	
	
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
	public SinkSourceManager(String taskId, SinkSource task, Map<String, String> pipeArgs) {
		super(taskId, pipeArgs);
		
		this.task = task;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void connect(PipeTasks pipeTasks) {
		Source source;
		
		// Get the input task. A sink only has one input, this corresponds to
		// pipe index 0.
		source = (Source) getInputTask(pipeTasks, 0, Source.class);
		
		// Cast the input feed to the correct type.
		// Connect the tasks.
		source.setSink(task);
		
		// Register the task as an output. A source only has one output, this
		// corresponds to pipe index 0.
		setOutputTask(pipeTasks, task, 0);
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
