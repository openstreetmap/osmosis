// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package com.bretth.osmosis.core.pgsql.v0_6;

import java.io.File;

import com.bretth.osmosis.core.pipeline.common.TaskConfiguration;
import com.bretth.osmosis.core.pipeline.common.TaskManager;
import com.bretth.osmosis.core.pipeline.common.TaskManagerFactory;
import com.bretth.osmosis.core.pipeline.v0_6.SinkManager;


/**
 * The task manager factory for a database dump writer.
 * 
 * @author Brett Henderson
 */
public class PostgreSqlDatasetDumpWriterFactory extends TaskManagerFactory {
	private static final String ARG_FILE_NAME = "directory";
	private static final String DEFAULT_FILE_PREFIX = "pgimport";
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected TaskManager createTaskManagerImpl(TaskConfiguration taskConfig) {
		String filePrefixString;
		File filePrefix;
		
		// Get the task arguments.
		filePrefixString = getStringArgument(taskConfig, ARG_FILE_NAME, DEFAULT_FILE_PREFIX);
		
		// Create a file object representing the directory from the file name provided.
		filePrefix = new File(filePrefixString);
		
		return new SinkManager(
			taskConfig.getId(),
			new PostgreSqlDatasetDumpWriter(filePrefix),
			taskConfig.getPipeArgs()
		);
	}
}
