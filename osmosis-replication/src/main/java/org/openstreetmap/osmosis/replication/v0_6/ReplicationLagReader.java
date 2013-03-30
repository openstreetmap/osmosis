// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.replication.v0_6;

import java.io.File;
import java.text.MessageFormat;
import java.util.logging.Logger;

import org.openstreetmap.osmosis.core.OsmosisRuntimeException;
import org.openstreetmap.osmosis.core.task.common.RunnableTask;
import org.openstreetmap.osmosis.core.util.FileBasedLock;
import org.openstreetmap.osmosis.core.util.PropertiesPersister;
import org.openstreetmap.osmosis.replication.common.ReplicationState;
import org.openstreetmap.osmosis.replication.common.ServerStateReader;
import org.openstreetmap.osmosis.replication.v0_6.impl.ReplicationDownloaderConfiguration;

/**
 * Compares the timestamp of a local replication directory and the timestamp on the 
 * HTTP server that is configured to provide the replication files. It calculates 
 * the number of seconds the local replication directory is behind the HTTP server
 * and prints it to stdout.
 * 
 * @author Peter Koerner
 */
public class ReplicationLagReader implements RunnableTask {
	private static final Logger LOG = Logger.getLogger(ReplicationLagReader.class.getName());
	private static final String LOCK_FILE_NAME = "download.lock";
	private static final String CONFIG_FILE = "configuration.txt";
	private static final String LOCAL_STATE_FILE = "state.txt";
	
	private boolean humanReadable;
	private File workingDirectory;
	private ServerStateReader serverStateReader;
	
	/**
	 * Creates a new instance.
	 * 
	 * @param workingDirectory
	 *            The directory containing configuration and tracking files.
	 * @param humanReadable
	 *            Print the replication lag in a Hours, Minutes and Seconds
	 *            instead of the raw number of seconds
	 */
	public ReplicationLagReader(File workingDirectory, boolean humanReadable) {
		this.workingDirectory = workingDirectory;
		this.humanReadable = humanReadable;
		
		serverStateReader = new ServerStateReader();
	}
	
	
	/**
	 * Calculate the replication lag and print it to stdout
	 */
	private void getLag() {
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
		
		// If local state isn't available we need to fail because no lag can be calculated.
		if (!localStatePersistor.exists()) {
			throw new OsmosisRuntimeException("Can't read local state.");
		}
		
		// fetch the local state from the file
		localState = new ReplicationState(localStatePersistor.loadMap());
		
		// extract the time of the local and the remote state files
		long local = localState.getTimestamp().getTime();
		long server = serverState.getTimestamp().getTime();
		
		// we assume the server being before the local state while calculating the difference
		long lag = (server - local) / 1000;
		
		// check if a human readable version is requested
		if (this.humanReadable) {
			
			if (lag > 86400) {
				
				// more than a day
				Object[] args = {
					new Long(lag / 86400), 
					new Long((lag % 86400) / 3600)
				};
				System.out.println(
					new MessageFormat("{0} day(s) and {1} hour(s)").format(args)
				);
				
			} else if (lag > 3600) {
				
				// morte than an hour
				Object[] args = {
					new Long(lag / 3600), 
					new Long((lag % 3600) / 60)
				};
				System.out.println(
					new MessageFormat("{0} hour(s) and {1} minute(s)").format(args)
				);
				
			} else if (lag > 60) {
				
				// more than a minute
				Object[] args = {
					new Long(lag / 60), 
					new Long(lag % 60)
				};
				System.out.println(
					new MessageFormat("{0} minute(s) and {1} second(s)").format(args)
				);
				
			} else {
				
				// just some seconds
				System.out.println(
					new MessageFormat("{0} second(s)").format(lag)
				);
				
			}
			
		} else {
			
			// print out the raw number of seconds
			System.out.println(lag);
			
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
			
			getLag();
			
			fileLock.unlock();
			
		} finally {
			fileLock.release();
		}
	}
}
