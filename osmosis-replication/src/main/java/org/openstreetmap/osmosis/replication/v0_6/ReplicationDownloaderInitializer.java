// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.replication.v0_6;

import java.io.File;

import org.openstreetmap.osmosis.core.OsmosisRuntimeException;
import org.openstreetmap.osmosis.core.task.common.RunnableTask;
import org.openstreetmap.osmosis.core.util.FileBasedLock;
import org.openstreetmap.osmosis.core.util.ResourceFileManager;


/**
 * Downloads a set of replication files from a HTTP server, and merges them into a
 * single output stream. It tracks the intervals covered by the current files
 * and stores the current timestamp between invocations forming the basis of a
 * replication mechanism.
 * 
 * @author Brett Henderson
 */
public class ReplicationDownloaderInitializer implements RunnableTask {
	
	private static final String LOCK_FILE_NAME = "download.lock";
	private static final String CONFIG_FILE_NAME = "configuration.txt";
	private static final String CONFIG_RESOURCE = "impl/replicationDownloaderConfiguration.txt";
	
	
	private File workingDirectory;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param workingDirectory
	 *            The directory containing configuration and tracking files.
	 */
	public ReplicationDownloaderInitializer(File workingDirectory) {
		this.workingDirectory = workingDirectory;
	}
	
	
	/**
	 * Initializes a working directory.
	 */
	private void initializeDirectory() {
		File configFile;
		
		ResourceFileManager resourceFileManager;
		
		// Instantiate utility objects.
		resourceFileManager = new ResourceFileManager();
		
		// Build file objects from file names.
		configFile = new File(workingDirectory, CONFIG_FILE_NAME);
		
		if (configFile.exists()) {
			throw new OsmosisRuntimeException("Config file " + CONFIG_FILE_NAME + " already exists.");
		}
		resourceFileManager.copyResourceToFile(getClass(), CONFIG_RESOURCE, configFile);
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
