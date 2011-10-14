// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.replication.v0_6;

import java.io.File;
import java.util.Date;

import org.openstreetmap.osmosis.core.OsmosisRuntimeException;
import org.openstreetmap.osmosis.core.task.common.RunnableTask;
import org.openstreetmap.osmosis.core.util.FileBasedLock;
import org.openstreetmap.osmosis.core.util.ResourceFileManager;
import org.openstreetmap.osmosis.replication.common.TimestampTracker;


/**
 * Downloads a set of change files from a HTTP server, and merges them into a
 * single output stream. It tracks the intervals covered by the current files
 * and stores the current timestamp between invocations forming the basis of a
 * replication mechanism.
 * 
 * @author Brett Henderson
 */
public class IntervalDownloaderInitializer implements RunnableTask {
	
	private static final String LOCK_FILE_NAME = "download.lock";
	private static final String CONFIG_FILE_NAME = "configuration.txt";
	private static final String TSTAMP_FILE_NAME = "timestamp.txt";
	private static final String TSTAMP_NEW_FILE_NAME = "timestamp-new.txt";
	private static final String CONFIG_RESOURCE = "impl/intervalConfiguration.txt";
	
	
	private File workingDirectory;
	private Date initialDate;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param workingDirectory
	 *            The directory containing configuration and tracking files.
	 * @param initialDate
	 *            The date to begin changeset downloads from.
	 */
	public IntervalDownloaderInitializer(File workingDirectory, Date initialDate) {
		this.workingDirectory = workingDirectory;
		this.initialDate = initialDate;
	}
	
	
	/**
	 * Initializes a working directory.
	 */
	private void initializeDirectory() {
		File configFile;
		File timestampFile;
		File newTimestampFile;
		
		ResourceFileManager resourceFileManager;
		
		// Instantiate utility objects.
		resourceFileManager = new ResourceFileManager();
		
		// Build file objects from file names.
		configFile = new File(workingDirectory, CONFIG_FILE_NAME);
		timestampFile = new File(workingDirectory, TSTAMP_FILE_NAME);
		newTimestampFile = new File(workingDirectory, TSTAMP_NEW_FILE_NAME);
		
		if (configFile.exists()) {
			throw new OsmosisRuntimeException("Config file " + CONFIG_FILE_NAME + " already exists.");
		}
		resourceFileManager.copyResourceToFile(getClass(), CONFIG_RESOURCE, configFile);
		
		if (timestampFile.exists()) {
			throw new OsmosisRuntimeException("Timestamp file " + TSTAMP_FILE_NAME + " already exists.");
		}
		
		new TimestampTracker(timestampFile, newTimestampFile).setTime(initialDate);
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
