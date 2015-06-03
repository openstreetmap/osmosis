// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.replication.v0_6;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.openstreetmap.osmosis.core.OsmosisRuntimeException;
import org.openstreetmap.osmosis.core.merge.common.ConflictResolutionMethod;
import org.openstreetmap.osmosis.core.pipeline.common.TaskRunner;
import org.openstreetmap.osmosis.core.task.v0_6.ChangeSink;
import org.openstreetmap.osmosis.core.task.v0_6.RunnableChangeSource;
import org.openstreetmap.osmosis.core.time.DateParser;
import org.openstreetmap.osmosis.core.util.FileBasedLock;
import org.openstreetmap.osmosis.replication.common.TimestampTracker;
import org.openstreetmap.osmosis.replication.v0_6.impl.ChangesetFileNameFormatter;
import org.openstreetmap.osmosis.replication.v0_6.impl.IntervalDownloaderConfiguration;
import org.openstreetmap.osmosis.set.v0_6.ChangeMerger;
import org.openstreetmap.osmosis.xml.common.CompressionMethod;
import org.openstreetmap.osmosis.xml.v0_6.XmlChangeReader;


/**
 * Downloads a set of change files from a HTTP server, and merges them into a
 * single output stream. It tracks the intervals covered by the current files
 * and stores the current timestamp between invocations forming the basis of a
 * replication mechanism.
 * 
 * @author Brett Henderson
 */
public class IntervalDownloader implements RunnableChangeSource {
	
	private static final Logger LOG = Logger.getLogger(IntervalDownloader.class.getName());
	
	
	private static final String LOCK_FILE = "download.lock";
	private static final String CONFIG_FILE = "configuration.txt";
	private static final String TSTAMP_FILE = "timestamp.txt";
	private static final String TSTAMP_NEW_FILE = "timestamp-new.txt";
	private static final String SERVER_TSTAMP_FILE = "timestamp.txt";
	
	
	private ChangeSink changeSink;
	private String taskId;
	private File workingDirectory;
	private DateParser dateParser;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param taskId
	 *            The identifier for the task, this is required because the
	 *            names of threads created by this task will use this name as a
	 *            prefix.
	 * @param workingDirectory
	 *            The directory containing configuration and tracking files.
	 */
	public IntervalDownloader(String taskId, File workingDirectory) {
		this.taskId = taskId;
		this.workingDirectory = workingDirectory;
		
		dateParser = new DateParser();
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setChangeSink(ChangeSink changeSink) {
		this.changeSink = changeSink;
	}
	
	
	/**
	 * Retrieves the latest timestamp from the server.
	 * 
	 * @param baseUrl
	 *            The url of the directory containing change files.
	 * @return The timestamp.
	 */
	private Date getServerTimestamp(URL baseUrl) {
		URL timestampUrl;
		
		try {
			timestampUrl = new URL(baseUrl, SERVER_TSTAMP_FILE);
		} catch (MalformedURLException e) {
			throw new OsmosisRuntimeException("The server timestamp URL could not be created.", e);
		}
		
		try {
			Date result;
			
			URLConnection connection = timestampUrl.openConnection();
			connection.setReadTimeout(15 * 60 * 1000); // timeout 15 minutes
			connection.setConnectTimeout(15 * 60 * 1000); // timeout 15 minutes
			
			try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
				result = dateParser.parse(reader.readLine());
			}
			
			return result;
			
		} catch (IOException e) {
			throw new OsmosisRuntimeException("Unable to read the timestamp from the server.", e);
		}
	}
	
	
	/**
	 * Downloads the file from the server with the specified name and writes it
	 * to a local temporary file.
	 * 
	 * @param fileName
	 *            The name of the file to download.
	 * @param baseUrl
	 *            The url of the directory containing change files.
	 * @return The temporary file containing the downloaded data.
	 */
	private File downloadChangesetFile(String fileName, URL baseUrl) {
		URL changesetUrl;
		
		try {
			changesetUrl = new URL(baseUrl, fileName);
		} catch (MalformedURLException e) {
			throw new OsmosisRuntimeException("The server file URL could not be created.", e);
		}
		
		try {
			File outputFile;
			
			// Open an input stream for the changeset file on the server.
			URLConnection connection = changesetUrl.openConnection();
			connection.setReadTimeout(15 * 60 * 1000); // timeout 15 minutes
			connection.setConnectTimeout(15 * 60 * 1000); // timeout 15 minutes
			try (BufferedInputStream source = new BufferedInputStream(connection.getInputStream(), 65536)) {

				// Create a temporary file to write the data to.
				outputFile = File.createTempFile("change", null);
				
				// Open a output stream for the destination file.
				try (BufferedOutputStream sink = new BufferedOutputStream(new FileOutputStream(outputFile), 65536)) {
					// Download the file.
					byte[] buffer = new byte[65536];
					for (int bytesRead = source.read(buffer); bytesRead > 0; bytesRead = source.read(buffer)) {
						sink.write(buffer, 0, bytesRead);
					}
				}
			}
			
			return outputFile;
			
		} catch (IOException e) {
			throw new OsmosisRuntimeException("Unable to read the changeset file " + fileName + " from the server.", e);
		}
	}
	
	
	/**
	 * Downloads the changeset files from the server and writes their contents
	 * to the output task.
	 */
	private void download() {
		IntervalDownloaderConfiguration configuration;
		TimestampTracker timestampTracker;
		ChangesetFileNameFormatter fileNameFormatter;
		Date currentTime;
		Date maximumTime;
		URL baseUrl;
		int maxDownloadCount;
		int downloadCount;
		ArrayList<File> tmpFileList;
		ArrayList<RunnableChangeSource> tasks;
		ArrayList<TaskRunner> taskRunners;
		boolean tasksSuccessful;
		
		// Instantiate utility objects.
		configuration = new IntervalDownloaderConfiguration(new File(workingDirectory, CONFIG_FILE));
		timestampTracker = new TimestampTracker(
			new File(workingDirectory, TSTAMP_FILE),
			new File(workingDirectory, TSTAMP_NEW_FILE)
		);
		fileNameFormatter = new ChangesetFileNameFormatter(
			configuration.getChangeFileBeginFormat(),
			configuration.getChangeFileEndFormat()
		);
		
		// Create the base url.
		try {
			baseUrl = new URL(configuration.getBaseUrl());
		} catch (MalformedURLException e) {
			throw new OsmosisRuntimeException(
					"Unable to convert URL string (" + configuration.getBaseUrl() + ") into a URL.", e);
		}
		
		tmpFileList = new ArrayList<File>();
		
		// Load the current time from the timestamp tracking file.
		currentTime = timestampTracker.getTime();
		
		// Load the latest timestamp from the server.
		maximumTime = getServerTimestamp(baseUrl);
		
		// Process until all files have been retrieved from the server.
		maxDownloadCount = configuration.getMaxDownloadCount();
		downloadCount = 0;
		while ((maxDownloadCount == 0 || downloadCount < maxDownloadCount) && currentTime.before(maximumTime)) {
			Date nextTime;
			String downloadFileName;
			
			// Calculate the end of the next time interval.
			nextTime = new Date(currentTime.getTime() + configuration.getIntervalLength());
			
			// Generate the filename to be retrieved from the server.
			downloadFileName = fileNameFormatter.generateFileName(currentTime, nextTime);
			
			// Download the changeset from the server.
			tmpFileList.add(downloadChangesetFile(downloadFileName, baseUrl));
			
			// Move the current time to the next interval.
			currentTime = nextTime;
			
			// Increment the current download count.
			downloadCount++;
		}
		
		// Generate a set of tasks for loading the change files and merge them
		// into a single change stream.
		tasks = new ArrayList<RunnableChangeSource>();
		for (File tmpFile : tmpFileList) {
			XmlChangeReader changeReader;
			
			// Generate a change reader task for the current task.
			changeReader = new XmlChangeReader(
				tmpFile,
				true,
				CompressionMethod.GZip
			);
			
			// If tasks already exist, a change merge task must be used to merge
			// existing output with this task output, otherwise this task can be
			// added to the list directly.
			if (tasks.size() > 0) {
				ChangeMerger changeMerger;
				
				// Create a new change merger merging the last task output with the current task.
				changeMerger = new ChangeMerger(ConflictResolutionMethod.LatestSource, 10);
				
				// Connect the inputs of this merger to the most recent change
				// output and the new change output.
				tasks.get(tasks.size() - 1).setChangeSink(changeMerger.getChangeSink(0));
				changeReader.setChangeSink(changeMerger.getChangeSink(1));
				
				tasks.add(changeReader);
				tasks.add(changeMerger);
				
			} else {
				tasks.add(changeReader);
			}
		}
		
		// We only need to execute sub-threads if tasks exist, otherwise we must
		// notify the sink that we have completed.
		if (tasks.size() > 0) {
			// Connect the last task to the change sink.
			tasks.get(tasks.size() - 1).setChangeSink(changeSink);
			
			// Create task runners for each of the tasks to provide thread
			// management.
			taskRunners = new ArrayList<TaskRunner>(tasks.size());
			for (int i = 0; i < tasks.size(); i++) {
				taskRunners.add(new TaskRunner(tasks.get(i), "Thread-" + taskId + "-worker" + i));
			}
			
			// Launch all of the tasks.
			for (int i = 0; i < taskRunners.size(); i++) {
				TaskRunner taskRunner;
				
				taskRunner = taskRunners.get(i);
				
				LOG.fine("Launching changeset worker + " + i + " in a new thread.");
				
				taskRunner.start();
			}
			
			// Wait for all the tasks to complete.
			tasksSuccessful = true;
			for (int i = 0; i < taskRunners.size(); i++) {
				TaskRunner taskRunner;
				
				taskRunner = taskRunners.get(i);
				
				LOG.fine("Waiting for changeset worker " + i + " to complete.");
				
				try {
					taskRunner.join();
				} catch (InterruptedException e) {
					// We are already in an error condition so log and continue.
					LOG.log(Level.WARNING, "The wait for task completion was interrupted.", e);
				}
				
				if (!taskRunner.isSuccessful()) {
					LOG.log(Level.SEVERE, "Changeset worker " + i + " failed", taskRunner.getException());
					
					tasksSuccessful = false;
				}
			}
			
		} else {
			changeSink.complete();
			tasksSuccessful = true;
		}
		
		// Remove the temporary files.
		for (File tmpFile : tmpFileList) {
			if (!tmpFile.delete()) {
				LOG.warning("Unable to delete file " + tmpFile.getName());
			}
		}
		
		if (!tasksSuccessful) {
			throw new OsmosisRuntimeException("One or more changeset workers failed.");
		}
		
		// Update the timestamp tracker.
		timestampTracker.setTime(currentTime);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void run() {
		FileBasedLock fileLock;
		
		fileLock = new FileBasedLock(new File(workingDirectory, LOCK_FILE));
		
		try {
			changeSink.initialize(Collections.<String, Object>emptyMap());
			
			fileLock.lock();
			
			download();
			
			fileLock.unlock();
			
		} finally {
			changeSink.close();
			fileLock.close();
		}
	}
}
