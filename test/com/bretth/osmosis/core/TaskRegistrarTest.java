// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package com.bretth.osmosis.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.bretth.osmosis.core.pipeline.common.TaskConfiguration;
import com.bretth.osmosis.core.pipeline.common.TaskManager;
import com.bretth.osmosis.core.pipeline.common.TaskManagerFactoryRegister;
import com.bretth.osmosis.core.pipeline.v0_5.ChangeSinkManager;
import com.bretth.osmosis.core.pipeline.v0_5.SinkManager;


/**
 * Tests the TaskRegistrar class.
 * 
 * @author Brett Henderson
 */
public class TaskRegistrarTest {
	
	private TaskManagerFactoryRegister createTaskManagerFactoryRegister() {
		return createTaskManagerFactoryRegister(new ArrayList<String>());
	}
	
	
	private TaskManagerFactoryRegister createTaskManagerFactoryRegister(List<String> plugins) {
		TaskRegistrar taskRegistrar;
		
		taskRegistrar = new TaskRegistrar();
		
		taskRegistrar.initialize(plugins);
		
		return taskRegistrar.getFactoryRegister();
	}
	
	
	/**
	 * Validates the standard class factory registration.
	 */
	@Test
	public void testDefaultRegistration() {
		TaskManager taskManager;
		TaskConfiguration taskConfig;
		
		taskConfig = new TaskConfiguration("myId", "write-null", new HashMap<String, String>(), new HashMap<String, String>(), null);
		
		// Register default tasks only and load the write-null task.
		taskManager = createTaskManagerFactoryRegister().getInstance(taskConfig.getType()).createTaskManager(taskConfig);
		
		Assert.assertEquals("Incorrect task manager created.", SinkManager.class, taskManager.getClass());
	}
	
	
	/**
	 * Validates the plugin class factory registration.
	 */
	@Test
	public void testPluginRegistration() {
		TaskManager taskManager;
		TaskConfiguration taskConfig;
		
		taskConfig = new TaskConfiguration("myId", "my-plugin-task", new HashMap<String, String>(), new HashMap<String, String>(), null);
		
		// Register the test plugin and load its task.
		taskManager = createTaskManagerFactoryRegister(Arrays.asList("com.bretth.osmosis.core.MyPluginLoader")).getInstance(taskConfig.getType()).createTaskManager(taskConfig);
		
		Assert.assertEquals("Incorrect task manager created.", ChangeSinkManager.class, taskManager.getClass());
	}
}
