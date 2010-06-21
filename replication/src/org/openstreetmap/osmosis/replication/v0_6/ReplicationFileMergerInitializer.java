// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.replication.v0_6;

import java.io.File;

import org.openstreetmap.osmosis.core.OsmosisRuntimeException;
import org.openstreetmap.osmosis.core.task.common.RunnableTask;
import org.openstreetmap.osmosis.core.util.FileBasedLock;
import org.openstreetmap.osmosis.core.util.ResourceFileManager;


/**
 * Initialises the working directory for the replication file merger task.
 */
public class ReplicationFileMergerInitializer implements RunnableTask {
	
	private static final String LOCK_FILE_NAME = "download.lock";
	private static final String CONFIG_FILE_NAME = "configuration.txt";
	private static final String CONFIG_RESOURCE = "impl/replicationFileMergerConfiguration.txt";
	private static final String DATA_DIRECTORY = "data";
	
	
	private File workingDirectory;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param workingDirectory
	 *            The directory containing configuration and tracking files.
	 */
	public ReplicationFileMergerInitializer(File workingDirectory) {
		this.workingDirectory = workingDirectory;
	}
	
	
	/**
	 * Initializes a working directory.
	 */
	private void initializeDirectory() {
		File configFile;
		File dataDirectory;
		
		ResourceFileManager resourceFileManager;
		
		// Instantiate utility objects.
		resourceFileManager = new ResourceFileManager();
		
		// Build file objects from file names.
		configFile = new File(workingDirectory, CONFIG_FILE_NAME);
		
		// Copy the template configuration file into the working directory.
		if (configFile.exists()) {
			throw new OsmosisRuntimeException("Config file " + configFile + " already exists.");
		}
		resourceFileManager.copyResourceToFile(getClass(), CONFIG_RESOURCE, configFile);
		
		// Create the data directory.
		dataDirectory = new File(workingDirectory, DATA_DIRECTORY);
		if (dataDirectory.exists()) {
			throw new OsmosisRuntimeException("Data directory " + dataDirectory + " already exists.");
		}
		if (!dataDirectory.mkdir()) {
			throw new OsmosisRuntimeException("Unable to create data directory " + dataDirectory + ".");
		}
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void run() {
		FileBasedLock fileLock;
		
		fileLock = new FileBasedLock(new File(workingDirectory, LOCK_FILE_NAME));
		
		try {
			fileLock.lock();
			
			initializeDirectory();
			
			fileLock.unlock();
			
		} finally {
			fileLock.release();
		}
	}
}
