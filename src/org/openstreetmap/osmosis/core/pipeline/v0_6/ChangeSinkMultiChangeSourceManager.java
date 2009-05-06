// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.pipeline.v0_6;

import java.util.Map;

import org.openstreetmap.osmosis.core.pipeline.common.PassiveTaskManager;
import org.openstreetmap.osmosis.core.pipeline.common.PipeTasks;
import org.openstreetmap.osmosis.core.task.v0_6.ChangeSinkMultiChangeSource;
import org.openstreetmap.osmosis.core.task.v0_6.ChangeSource;


/**
 * A task manager implementation for task performing change sink and multi
 * change source functionality.
 * 
 * @author Brett Henderson
 */
public class ChangeSinkMultiChangeSourceManager extends PassiveTaskManager {
	private ChangeSinkMultiChangeSource task;
	
	
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
	public ChangeSinkMultiChangeSourceManager(
			String taskId, ChangeSinkMultiChangeSource task, Map<String, String> pipeArgs) {
		super(taskId, pipeArgs);
		
		this.task = task;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void connect(PipeTasks pipeTasks) {
		ChangeSource source;
		int taskSourceCount;
		
		// Get the input task. A sink only has one input, this corresponds to
		// pipe index 0.
		source = (ChangeSource) getInputTask(pipeTasks, 0, ChangeSource.class);
		
		// Cast the input feed to the correct type.
		// Connect the tasks.
		source.setChangeSink(task);
		
		// Register all the sources provided by this task as outputs.
		taskSourceCount = task.getChangeSourceCount();
		for (int i = 0; i < taskSourceCount; i++) {
			setOutputTask(pipeTasks, task.getChangeSource(i), i);
		}
	}
}
