// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.merge.v0_6;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.openstreetmap.osmosis.core.OsmosisRuntimeException;
import org.openstreetmap.osmosis.core.apidb.v0_6.impl.ReplicationFileSequenceFormatter;
import org.openstreetmap.osmosis.core.apidb.v0_6.impl.ReplicationState;
import org.openstreetmap.osmosis.core.container.v0_6.ChangeContainer;
import org.openstreetmap.osmosis.core.merge.v0_6.impl.ReplicationDownloaderConfiguration;
import org.openstreetmap.osmosis.core.merge.v0_6.impl.ServerStateReader;
import org.openstreetmap.osmosis.core.task.v0_6.ChangeSink;
import org.openstreetmap.osmosis.core.task.v0_6.RunnableChangeSource;
import org.openstreetmap.osmosis.core.util.FileBasedLock;
import org.openstreetmap.osmosis.core.util.PropertiesPersister;
import org.openstreetmap.osmosis.core.xml.common.CompressionMethod;
import org.openstreetmap.osmosis.core.xml.v0_6.XmlChangeReader;


/**
 * Downloads a set of replication files from a HTTP server, and merges them into a
 * single output stream. It tracks the intervals covered by the current files
 * and stores the current timestamp between invocations forming the basis of a
 * replication mechanism.
 * 
 * @author Brett Henderson
 */
public class ReplicationDownloader implements RunnableChangeSource {
	
	private static final Logger LOG = Logger.getLogger(IntervalDownloader.class.getName());
	private static final String LOCK_FILE = "download.lock";
	private static final String CONFIG_FILE = "configuration.txt";
	private static final String LOCAL_STATE_FILE = "state.txt";
	
	
	private ChangeSink changeSink;
	private File workingDirectory;
	private ReplicationFileSequenceFormatter sequenceFormatter;
	private ServerStateReader serverStateReader;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param workingDirectory
	 *            The directory containing configuration and tracking files.
	 */
	public ReplicationDownloader(File workingDirectory) {
		this.workingDirectory = workingDirectory;
		
		sequenceFormatter = new ReplicationFileSequenceFormatter();
		serverStateReader = new ServerStateReader();
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setChangeSink(ChangeSink changeSink) {
		this.changeSink = changeSink;
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
			inputStream = changesetUrl.openStream();
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
	
	
	private void processReplicationFile(File replicationFile) {
		final ChangeSink localChangeSink = changeSink;
		
		try {
			XmlChangeReader xmlReader;
			
			// Send the contents of the replication file to the sink but suppress the complete
			// and release methods.
			xmlReader = new XmlChangeReader(replicationFile, true, CompressionMethod.GZip);
			
			xmlReader.setChangeSink(new ChangeSink() {
				ChangeSink suppressedChangeSink = localChangeSink;
				
				@Override
				public void process(ChangeContainer change) {
					suppressedChangeSink.process(change);
				}
				@Override
				public void complete() {
					// Suppress the call.
				}
				@Override
				public void release() {
					// Suppress the call.
				}});
			
			xmlReader.run();
			
		} finally {
			if (!replicationFile.delete()) {
				LOG.warning("Unable to delete file " + replicationFile.getName());
			}
		}
	}
	
	
	private void download() {
		ReplicationDownloaderConfiguration configuration;
		ReplicationState serverState;
		ReplicationState localState;
		URL baseUrl;
		PropertiesPersister localStatePersistor;
		Properties localStateProperties;
		long replicationCount;
		
		// Instantiate utility objects.
		configuration = new ReplicationDownloaderConfiguration(new File(workingDirectory, CONFIG_FILE));
		
		// Determine the location of replication files.
		baseUrl = configuration.getBaseUrl();
		
		// Obtain the server state.
		serverState = serverStateReader.getServerState(baseUrl);
		
		// Build the local state persister which is used for both loading and storing local state.
		localStatePersistor = new PropertiesPersister(
				new File(workingDirectory, LOCAL_STATE_FILE),
				new File(workingDirectory, "tmp" + LOCAL_STATE_FILE));
		
		// If local state isn't available we need to copy server state to be the initial local state
		// then exit.
		if (localStatePersistor.exists()) {
			localStateProperties = localStatePersistor.load();
			localState = new ReplicationState(localStateProperties);
			
			// Determine how many replications are available.
			replicationCount = serverState.getSequenceNumber() - localState.getSequenceNumber();
			replicationCount = Math.min(replicationCount, configuration.getMaxDownloadCount());
			
			// Download all files and send their contents to the sink.
			for (long i = 0; i < replicationCount; i++) {
				File replicationFile;
				long sequenceNumber;
				
				// Download the next replication file to a temporary file.
				sequenceNumber = localState.getSequenceNumber() + i + 1;
				replicationFile = downloadReplicationFile(sequenceFormatter.getFormattedName(sequenceNumber), baseUrl);
				
				// Process the file and send its contents to the sink.
				processReplicationFile(replicationFile);
				
				// Update the local state to reflect the server state just processed.
				localState = serverStateReader.getServerState(baseUrl, sequenceNumber);
			}
			
		} else {
			localState = serverState;
		}
		
		// Persist the local state.
		localStateProperties = new Properties();
		localState.store(localStateProperties);
		localStatePersistor.store(localStateProperties);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void run() {
		FileBasedLock fileLock;
		
		fileLock = new FileBasedLock(new File(workingDirectory, LOCK_FILE));
		
		try {
			fileLock.lock();
			
			download();
			changeSink.complete();
			
			fileLock.unlock();
			
		} finally {
			changeSink.release();
			fileLock.release();
		}
	}
}
