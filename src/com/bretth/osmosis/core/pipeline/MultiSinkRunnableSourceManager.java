package com.bretth.osmosis.core.pipeline;

import java.util.Map;

import com.bretth.osmosis.core.task.MultiSinkRunnableSource;
import com.bretth.osmosis.core.task.Sink;
import com.bretth.osmosis.core.task.Source;


/**
 * A task manager implementation for MultiSinkRunnableSource task implementations.
 * 
 * @author Brett Henderson
 */
public class MultiSinkRunnableSourceManager extends ActiveTaskManager {
	private MultiSinkRunnableSource task;
	
	
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
	public MultiSinkRunnableSourceManager(String taskId, MultiSinkRunnableSource task, Map<String, String> pipeArgs) {
		super(taskId, pipeArgs);
		
		this.task = task;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void connect(PipeTasks pipeTasks) {
		// A multi sink receives multiple streams of data, so we must connect
		// them up one by one.
		for (int i = 0; i < task.getSinkCount(); i++) {
			Sink sink;
			Source source;
			
			// Retrieve the next sink.
			sink = task.getSink(i);
			
			// Retrieve the appropriate source.
			source = (Source) getInputTask(pipeTasks, i, Source.class);
			
			// Connect the tasks.
			source.setSink(sink);
		}
		
		// Register the source as an output task.
		setOutputTask(pipeTasks, task, 0);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Runnable getTask() {
		return task;
	}
}
