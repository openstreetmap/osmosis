// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.replication.common;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.openstreetmap.osmosis.core.OsmosisRuntimeException;
import org.openstreetmap.osmosis.core.OsmosisConstants;


/**
 * Retrieves replication state files from the server hosting replication data.
 */
public class ServerStateReader {
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
		
		try {
			stateUrl = new URL(baseUrl, stateFile);
		} catch (MalformedURLException e) {
			throw new OsmosisRuntimeException("The server timestamp URL could not be created.", e);
		}
		
		try {
			Properties stateProperties;
			Map<String, String> stateMap;
			ReplicationState state;
			
			URLConnection connection = stateUrl.openConnection();
			connection.setReadTimeout(15 * 60 * 1000); // timeout 15 minutes
			connection.setConnectTimeout(15 * 60 * 1000); // timeout 15 minutes
			connection.setRequestProperty("User-Agent", "Osmosis/" + OsmosisConstants.VERSION);
			try (BufferedReader reader  = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
				stateProperties = new Properties();
				stateProperties.load(reader);
			} catch (IOException e) {
				throw new OsmosisRuntimeException("Unable to read the state from the server.", e);
			}
			
			stateMap = new HashMap<String, String>();
			for (Entry<Object, Object> property : stateProperties.entrySet()) {
				stateMap.put((String) property.getKey(), (String) property.getValue());
			}
			
			state = new ReplicationState(stateMap);
			
			return state;
			
		} catch (IOException e) {
			throw new OsmosisRuntimeException("Unable to read the state from the server.", e);
		}
	}
}
