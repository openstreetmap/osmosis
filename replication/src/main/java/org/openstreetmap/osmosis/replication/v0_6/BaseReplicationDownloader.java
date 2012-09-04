// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.replication.v0_6;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.openstreetmap.osmosis.core.OsmosisRuntimeException;
import org.openstreetmap.osmosis.core.task.common.RunnableTask;
import org.openstreetmap.osmosis.core.util.FileBasedLock;
import org.openstreetmap.osmosis.core.util.PropertiesPersister;
import org.openstreetmap.osmosis.replication.common.ReplicationSequenceFormatter;
import org.openstreetmap.osmosis.replication.common.ReplicationState;
import org.openstreetmap.osmosis.replication.common.ServerStateReader;
import org.openstreetmap.osmosis.replication.v0_6.impl.ReplicationDownloaderConfiguration;
import org.openstreetmap.osmosis.xml.common.CompressionMethod;
import org.openstreetmap.osmosis.xml.v0_6.XmlChangeReader;


/**
 * This class downloads a set of replication files from a HTTP server and tracks the progress of
 * which files have already been processed. The actual processing of changeset files is performed by
 * sub-classes. This class forms the basis of a replication mechanism.
 * 
 * @author Brett Henderson
 */
public abstract class BaseReplicationDownloader implements RunnableTask {
	
	private static final Logger LOG = Logger.getLogger(BaseReplicationDownloader.class.getName());
	private static final String LOCK_FILE = "download.lock";
	private static final String CONFIG_FILE = "configuration.txt";
	private static final String LOCAL_STATE_FILE = "state.txt";
	
	
	private File workingDirectory;
	private ReplicationSequenceFormatter sequenceFormatter;
	private ServerStateReader serverStateReader;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param workingDirectory
	 *            The directory containing configuration and tracking files.
	 */
	public BaseReplicationDownloader(File workingDirectory) {
		this.workingDirectory = workingDirectory;
		
		sequenceFormatter = new ReplicationSequenceFormatter(9, 3);
		serverStateReader = new ServerStateReader();
	}
	
	
	/**
	 * Provides sub-classes with access to the working directory.
	 * 
	 * @return The working directory for the task.
	 */
	protected File getWorkingDirectory() {
		return workingDirectory;
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
	private File downloadReplicationFile(String fileName, URL baseUrl) {
		URL changesetUrl;
		InputStream inputStream = null;
		OutputStream outputStream = null;
		
		try {
			changesetUrl = new URL(baseUrl, fileName);
		} catch (MalformedURLException e) {
			throw new OsmosisRuntimeException("The server file URL could not be created.", e);
		}
		
		try {
			BufferedInputStream source;
			BufferedOutputStream sink;
			File outputFile;
			byte[] buffer;
			
			// Open an input stream for the changeset file on the server.
			URLConnection connection = changesetUrl.openConnection();
			connection.setReadTimeout(15 * 60 * 1000); // timeout 15 minutes
			connection.setConnectTimeout(15 * 60 * 1000); // timeout 15 minutes
			inputStream = connection.getInputStream();
			source = new BufferedInputStream(inputStream, 65536);
			
			// Create a temporary file to write the data to.
			outputFile = File.createTempFile("change", null);
			
			// Open a output stream for the destination file.
			outputStream = new FileOutputStream(outputFile);
			sink = new BufferedOutputStream(outputStream, 65536);
			
			// Download the file.
			buffer = new byte[65536];
			for (int bytesRead = source.read(buffer); bytesRead > 0; bytesRead = source.read(buffer)) {
				sink.write(buffer, 0, bytesRead);
			}
			sink.flush();
			
			// Clean up all file handles.
			inputStream.close();
			inputStream = null;
			outputStream.close();
			outputStream = null;
			
			return outputFile;
			
		} catch (IOException e) {
			throw new OsmosisRuntimeException("Unable to read the changeset file " + fileName + " from the server.", e);
		} finally {
			try {
				if (inputStream != null) {
					inputStream.close();
				}
			} catch (IOException e) {
				// We are already in an error condition so log and continue.
				LOG.log(Level.WARNING, "Unable to changeset download stream.", e);
			}
			try {
				if (outputStream != null) {
					outputStream.close();
				}
			} catch (IOException e) {
				// We are already in an error condition so log and continue.
				LOG.log(Level.WARNING, "Unable to changeset output stream.", e);
			}
		}
	}
	
	
	private void processReplicationFile(File replicationFile, ReplicationState replicationState) {
		try {
			XmlChangeReader xmlReader;
			
			// Send the contents of the replication file to the sink but suppress the complete
			// and release methods.
			xmlReader = new XmlChangeReader(replicationFile, true, CompressionMethod.GZip);
			
			// Delegate to the sub-class to process the xml.
			processChangeset(xmlReader, replicationState);
			
		} finally {
			if (!replicationFile.delete()) {
				LOG.warning("Unable to delete file " + replicationFile.getName());
			}
		}
	}


	/**
	 * Determines the maximum timestamp of data to be downloaded during this invocation. This may be
	 * overriden by sub-classes, but the sub-classes must call this implemention first and then
	 * limit the maximum timestamp further if needed. A sub-class may never increase the maximum
	 * timestamp beyond that calculated by this method.
	 * 
	 * @param configuration
	 *            The configuration.
	 * @param serverTimestamp
	 *            The timestamp of the latest data on the server.
	 * @param localTimestamp
	 *            The timestamp of the most recently downloaded data.
	 * @return The maximum timestamp for this invocation.
	 */
	protected Date calculateMaximumTimestamp(ReplicationDownloaderConfiguration configuration, Date serverTimestamp,
			Date localTimestamp) {
		Date maximumTimestamp;
		
		maximumTimestamp = serverTimestamp;
		
		// Limit the duration according to the maximum defined in the configuration.
		if (configuration.getMaxInterval() > 0) {
			if ((serverTimestamp.getTime() - localTimestamp.getTime())
				> configuration.getMaxInterval()) {
				maximumTimestamp = new Date(localTimestamp.getTime() + configuration.getMaxInterval());
			}
		}
		
		LOG.finer("Maximum timestamp is " + maximumTimestamp);
		
		return maximumTimestamp;
	}
	
	
	private ReplicationState download(ReplicationDownloaderConfiguration configuration, ReplicationState serverState,
			ReplicationState initialLocalState) {
		URL baseUrl;
		ReplicationState localState;
		Date maximumDownloadTimestamp;
		
		localState = initialLocalState;
		
		// Determine the location of download files.
		baseUrl = configuration.getBaseUrl();
		
		// Determine the maximum timestamp that can be downloaded.
		maximumDownloadTimestamp =
			calculateMaximumTimestamp(configuration, serverState.getTimestamp(), localState.getTimestamp());
		LOG.fine("The maximum timestamp to be downloaded is " + maximumDownloadTimestamp + ".");
		
		// Download all files and send their contents to the sink.
		while (localState.getSequenceNumber() < serverState.getSequenceNumber()) {
			File replicationFile;
			long sequenceNumber;
			ReplicationState fileReplicationState;
			
			// Check to see if our local state has already reached the maximum
			// allowable timestamp. This will typically occur if a job is run
			// again before new data becomes available, or if an implementation
			// of this class (eg. ReplicationFileMerger) is waiting for a full
			// time period of data to become available before processing.
			if (localState.getTimestamp().compareTo(maximumDownloadTimestamp) >= 0) {
				break;
			}
			
			// Calculate the next sequence number.
			sequenceNumber = localState.getSequenceNumber() + 1;
			LOG.finer("Processing replication sequence " + sequenceNumber + ".");
			
			// Get the state associated with the next file.
			fileReplicationState = serverStateReader.getServerState(baseUrl, sequenceNumber);
			
			// Ensure that the next state is within the allowable timestamp
			// range. We must stop if the next data takes us beyond the maximum
			// timestamp. This will either occur if a maximum download time
			// duration limit has been imposed, or if a time-aligned boundary
			// has been reached.
			if (fileReplicationState.getTimestamp().compareTo(maximumDownloadTimestamp) > 0) {
				// We will always allow at least one replication interval
				// through to deal with the case where a single interval exceeds
				// the maximum duration. This can happen if the source data has
				// a long time gap between two intervals due to system downtime.
				if (localState.getSequenceNumber() != initialLocalState.getSequenceNumber()) {
					break;
				}
			}
			
			// Download the next replication file to a temporary file.
			replicationFile =
				downloadReplicationFile(sequenceFormatter.getFormattedName(sequenceNumber, ".osc.gz"), baseUrl);
			
			// Process the file and send its contents to the sink.
			processReplicationFile(replicationFile, fileReplicationState);
			
			// Update the local state to reflect the file state just processed.
			localState = fileReplicationState;
		}
		
		return localState;
	}
	
	
	private void runImpl() {
		try {
			ReplicationDownloaderConfiguration configuration;
			ReplicationState serverState;
			ReplicationState localState;
			PropertiesPersister localStatePersistor;
			
			// Instantiate utility objects.
			configuration = new ReplicationDownloaderConfiguration(new File(workingDirectory, CONFIG_FILE));
			
			// Obtain the server state.
			LOG.fine("Reading current server state.");
			serverState = serverStateReader.getServerState(configuration.getBaseUrl());
			
			// Build the local state persister which is used for both loading and storing local state.
			localStatePersistor = new PropertiesPersister(new File(workingDirectory, LOCAL_STATE_FILE));
			
			// Begin processing.
			processInitialize(Collections.<String, Object>emptyMap());
			
			// If local state isn't available we need to copy server state to be the initial local state
			// then exit.
			if (localStatePersistor.exists()) {
				localState = new ReplicationState(localStatePersistor.loadMap());
				
				// Download and process the replication files.
				localState = download(configuration, serverState, localState);
				
			} else {
				localState = serverState;
				
				processInitializeState(localState);
			}
			
			// Commit downstream changes.
			processComplete();
			
			// Persist the local state.
			localStatePersistor.store(localState.store());
			
		} finally {
			processRelease();
		}
	}
	
	
	/**
	 * This is called prior to any processing being performed. It allows any
	 * setup activities to be performed.
	 * 
	 * @param metaData
	 *            The meta data associated with this processing request (empty
	 *            in the current implementation).
	 */
	protected abstract void processInitialize(Map<String, Object> metaData);
	
	
	/**
	 * Invoked once during the first execution run to allow initialisation based on the initial
	 * replication state downloaded from the server.
	 * 
	 * @param initialState
	 *            The first server state.
	 */
	protected abstract void processInitializeState(ReplicationState initialState);
	
	
	/**
	 * Processes the changeset.
	 * 
	 * @param xmlReader
	 *            The changeset reader initialised to point to the changeset file.
	 * @param replicationState
	 *            The replication state associated with the changeset file.
	 */
	protected abstract void processChangeset(XmlChangeReader xmlReader, ReplicationState replicationState);


	/**
	 * This is implemented by sub-classes and is called when all changesets have been processed.
	 * This should perform any completion tasks such as committing changes to a database.
	 */
	protected abstract void processComplete();


	/**
	 * This is implemented by sub-classes and is called and the completion of all processing
	 * regardless of whether it was successful or not. This should perform any cleanup tasks such as
	 * closing files or releasing database connections.
	 */
	protected abstract void processRelease();
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void run() {
		FileBasedLock fileLock;
		
		fileLock = new FileBasedLock(new File(workingDirectory, LOCK_FILE));
		
		try {
			fileLock.lock();
			
			runImpl();
			
			fileLock.unlock();
			
		} finally {
			fileLock.release();
		}
	}
}
