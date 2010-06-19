// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.merge.v0_6.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.openstreetmap.osmosis.core.OsmosisRuntimeException;
import org.openstreetmap.osmosis.core.replication.ReplicationSequenceFormatter;
import org.openstreetmap.osmosis.core.replication.ReplicationState;


/**
 * Retrieves replication state files from the server hosting replication data.
 */
public class ServerStateReader {
	private static final Logger LOG = Logger.getLogger(ServerStateReader.class.getName());
	private static final String SERVER_STATE_FILE = "state.txt";
	private static final String SEQUENCE_STATE_FILE_SUFFIX = ".state.txt";
	
	
	private ReplicationSequenceFormatter sequenceFormatter;
	
	
	/**
	 * Creates a new instance.
	 */
	public ServerStateReader() {
		sequenceFormatter = new ReplicationSequenceFormatter(9, 3);
	}
	
	
	/**
	 * Retrieves the latest state from the server.
	 * 
	 * @param baseUrl
	 *            The url of the directory containing change files.
	 * @return The state.
	 */
	public ReplicationState getServerState(URL baseUrl) {
		return getServerState(baseUrl, SERVER_STATE_FILE);
	}
	
	
	/**
	 * Retrieves the specified state from the server.
	 * 
	 * @param baseUrl
	 *            The url of the directory containing change files.
	 * @param sequenceNumber
	 *            The sequence number of the state to be retrieved from the server.
	 * @return The state.
	 */
	public ReplicationState getServerState(URL baseUrl, long sequenceNumber) {
		return getServerState(baseUrl, sequenceFormatter.getFormattedName(sequenceNumber, SEQUENCE_STATE_FILE_SUFFIX));
	}


	/**
	 * Retrieves the specified state from the server.
	 * 
	 * @param baseUrl
	 *            The url of the directory containing change files.
	 * @param stateFile
	 *            The state file to be retrieved.
	 * @return The state.
	 */
	private ReplicationState getServerState(URL baseUrl, String stateFile) {
		URL stateUrl;
		InputStream stateStream = null;
		
		try {
			stateUrl = new URL(baseUrl, stateFile);
		} catch (MalformedURLException e) {
			throw new OsmosisRuntimeException("The server timestamp URL could not be created.", e);
		}
		
		try {
			BufferedReader reader;
			Properties stateProperties;
			ReplicationState state;
			
			stateStream = stateUrl.openStream();
			
			reader = new BufferedReader(new InputStreamReader(stateStream));
			stateProperties = new Properties();
			stateProperties.load(reader);
			
			state = new ReplicationState(stateProperties);
			
			stateStream.close();
			stateStream = null;
			
			return state;
			
		} catch (IOException e) {
			throw new OsmosisRuntimeException("Unable to read the state from the server.", e);
		} finally {
			try {
				if (stateStream != null) {
					stateStream.close();
				}
			} catch (IOException e) {
				// We are already in an error condition so log and continue.
				LOG.log(Level.WARNING, "Unable to close state stream.", e);
			}
		}
	}
}
