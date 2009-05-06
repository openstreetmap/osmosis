// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.pipeline.v0_6;

import java.util.Map;

import org.openstreetmap.osmosis.core.pipeline.common.ActiveTaskManager;
import org.openstreetmap.osmosis.core.pipeline.common.PipeTasks;
import org.openstreetmap.osmosis.core.task.v0_6.ChangeSink;
import org.openstreetmap.osmosis.core.task.v0_6.ChangeSource;
import org.openstreetmap.osmosis.core.task.v0_6.MultiSinkMultiChangeSinkRunnableSource;
import org.openstreetmap.osmosis.core.task.v0_6.Sink;
import org.openstreetmap.osmosis.core.task.v0_6.Source;


/**
 * A task manager implementation for SinkChangeSinkSource task implementations.
 * 
 * @author Brett Henderson
 */
public class MultiSinkMultiChangeSinkRunnableSourceManager extends ActiveTaskManager {
	private MultiSinkMultiChangeSinkRunnableSource task;
	
	
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
	public MultiSinkMultiChangeSinkRunnableSourceManager(
			String taskId, MultiSinkMultiChangeSinkRunnableSource task, Map<String, String> pipeArgs) {
		super(taskId, pipeArgs);
		
		this.task = task;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void connect(PipeTasks pipeTasks) {
		// A multi sink receives multiple streams of data, so we must connect
		// them up one by one. In this case we will connect the sinks and then
		// the change sinks.
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
		for (int i = 0; i < task.getChangeSinkCount(); i++) {
			ChangeSink changeSink;
			ChangeSource changeSource;
			
			// Retrieve the next sink.
			changeSink = task.getChangeSink(i);
			
			// Retrieve the appropriate source.
			changeSource = (ChangeSource) getInputTask(
				pipeTasks,
				i + task.getSinkCount(),
				ChangeSource.class
			);
			
			// Connect the tasks.
			changeSource.setChangeSink(changeSink);
		}
		
		// Register the change source as an output task.
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
