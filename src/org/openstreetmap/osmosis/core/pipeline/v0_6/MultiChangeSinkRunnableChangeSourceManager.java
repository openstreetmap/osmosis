// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.pipeline.v0_6;

import java.util.Map;

import org.openstreetmap.osmosis.core.pipeline.common.ActiveTaskManager;
import org.openstreetmap.osmosis.core.pipeline.common.PipeTasks;
import org.openstreetmap.osmosis.core.task.v0_6.ChangeSink;
import org.openstreetmap.osmosis.core.task.v0_6.ChangeSource;
import org.openstreetmap.osmosis.core.task.v0_6.MultiChangeSinkRunnableChangeSource;


/**
 * A task manager implementation for MultiChangeSinkRunnableChangeSource task implementations.
 * 
 * @author Brett Henderson
 */
public class MultiChangeSinkRunnableChangeSourceManager extends ActiveTaskManager {
	private MultiChangeSinkRunnableChangeSource task;
	
	
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
	public MultiChangeSinkRunnableChangeSourceManager(
			String taskId, MultiChangeSinkRunnableChangeSource task, Map<String, String> pipeArgs) {
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
		for (int i = 0; i < task.getChangeSinkCount(); i++) {
			ChangeSink sink;
			ChangeSource source;
			
			// Retrieve the next sink.
			sink = task.getChangeSink(i);
			
			// Retrieve the appropriate source.
			source = (ChangeSource) getInputTask(pipeTasks, i, ChangeSource.class);
			
			// Connect the tasks.
			source.setChangeSink(sink);
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
