package com.bretth.osm.conduit.pipeline;

import java.util.Map;

import com.bretth.osm.conduit.task.Task;

public class Node {
	private TaskManager taskManager;
	private Task task;
	private Map<String, String> pipeArgs;
	
	
	public Node(String taskName, Map<String, String> taskArgs, Map<String, String> pipeArgs) {
		this.pipeArgs = pipeArgs;
		
		taskManager = TaskManager.getInstance(taskName);
		task = taskManager.createTask(taskArgs);
	}
	
	
	public void connect(Map<String, Task> pipeTasks) {
		taskManager.connectTask(task, pipeTasks, pipeArgs);
	}
	
	
	public void run() {
		taskManager.runTask(task);
	}
	
	
	public void waitForCompletion() {
		taskManager.waitOnTask(task);
	}
}
