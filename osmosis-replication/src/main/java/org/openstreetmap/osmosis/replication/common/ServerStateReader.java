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

import java.net.URI;
import java.net.Authenticator;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;

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

	// environment variable: export http_proxy="http://USER:PASSWORD@HOST:PORT"
	public static Map<String,String> getEnvProxy(String envvar)
	{
	Map<String,String> proxy = new HashMap<String,String>();
	String envproxy = System.getenv(envvar);
	if (envproxy != null) {
	    URI uri = URI.create(envproxy);
	    proxy.put("HOST", uri.getHost());
	    proxy.put("PORT", Integer.toString(uri.getPort()));
	    String userinfo = uri.getUserInfo();
	    if (userinfo != null) {
	        String[] userpassinfo = userinfo.split(":", 0);
	        proxy.put("USER", userpassinfo[0]);
	        if (userpassinfo.length > 1) {
	            proxy.put("PASSWORD", userpassinfo[1]);
	        }
	    }
	}
	return proxy;
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
		
		final Map<String,String> proxy = getEnvProxy("http_proxy");
	        if (proxy.containsKey("HOST")) {
	            System.setProperty("proxySet", "true");
	            System.setProperty("proxyHost", proxy.get("HOST"));
	            System.setProperty("proxyPort", proxy.get("PORT"));
	            if (proxy.containsKey("USER")) {
	                Authenticator.setDefault(new Authenticator() {
	                    @Override
	                    protected PasswordAuthentication getPasswordAuthentication() {
	                        return new PasswordAuthentication(proxy.get("USER"), proxy.get("PASSWORD").toCharArray());
	                    }
	                });
	            }
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
