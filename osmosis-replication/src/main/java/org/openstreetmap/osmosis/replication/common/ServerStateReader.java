// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.replication.common;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.openstreetmap.osmosis.core.OsmosisRuntimeException;


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
			Map<String, String> stateMap;
			ReplicationState state;
			
			URLConnection connection = stateUrl.openConnection();
			connection.setReadTimeout(15 * 60 * 1000); // timeout 15 minutes
			connection.setConnectTimeout(15 * 60 * 1000); // timeout 15 minutes
			stateStream = connection.getInputStream();
			
			reader = new BufferedReader(new InputStreamReader(stateStream));
			stateProperties = new Properties();
			stateProperties.load(reader);
			
			stateMap = new HashMap<String, String>();
			for (Entry<Object, Object> property : stateProperties.entrySet()) {
				stateMap.put((String) property.getKey(), (String) property.getValue());
			}
			
			state = new ReplicationState(stateMap);
			
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
