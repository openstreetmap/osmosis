// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package com.bretth.osmosis.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import org.junit.Assert;
import org.junit.Test;

import com.bretth.osmosis.core.cli.TaskConfiguration;
import com.bretth.osmosis.core.pipeline.common.TaskManager;
import com.bretth.osmosis.core.pipeline.common.TaskManagerFactory;
import com.bretth.osmosis.core.pipeline.v0_5.ChangeSinkManager;
import com.bretth.osmosis.core.pipeline.v0_5.SinkManager;


/**
 * Tests the TaskRegistrar class.
 * 
 * @author Brett Henderson
 */
public class TaskRegistrarTest {

	/**
	 * Validates the standard class factory registration.
	 */
	@SuppressWarnings("deprecation")
	@Test
	public void testDefaultRegistration() {
		TaskManager taskManager;
		
		// Initialse the task registrar with no plugins.
		TaskManagerFactory.clearAll();
		TaskRegistrar.initialize(new ArrayList<String>());
		
		taskManager = TaskManagerFactory.createTaskManager(new TaskConfiguration("myId", "write-null", new HashMap<String, String>(), new HashMap<String, String>(), null));
		
		Assert.assertEquals("Incorrect task manager created.", SinkManager.class, taskManager.getClass());
	}
	
	
	/**
	 * Validates the plugin class factory registration.
	 */
	@SuppressWarnings("deprecation")
	@Test
	public void testPluginRegistration() {
		TaskManager taskManager;
		
		// Initialse the task registrar with no plugins.
		TaskManagerFactory.clearAll();
		TaskRegistrar.initialize(Arrays.asList("com.bretth.osmosis.core.MyPluginLoader"));
		
		// Create a task from the plugin registered task list.
		taskManager = TaskManagerFactory.createTaskManager(new TaskConfiguration("myId", "my-plugin-task", new HashMap<String, String>(), new HashMap<String, String>(), null));
		
		Assert.assertEquals("Incorrect task manager created.", ChangeSinkManager.class, taskManager.getClass());
	}
}
