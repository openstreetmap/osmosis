// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.replication.common;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.openstreetmap.osmosis.core.OsmosisConstants;
import org.openstreetmap.osmosis.core.OsmosisRuntimeException;

/**
 * Cookie to be sent with all HTTP requests. The cookie is read from a file.
 *
 * @author Michael Reichert
 */
public class ReplicationCookie {
	private static final String COOKIE_FILE_NAME = "cookie.txt";
	
	private Path directory;
	private URL cookieStatusAPI;
	private String data;

	/**
	 * Create an invalid dummy cookie.
	 */
	public ReplicationCookie() {
		directory = null;
		cookieStatusAPI = null;
		data = null;
	}

	/**
	 * Creates an empty cookie.
	 *
	 * @param cookieDirectory directory to read the cookie.txt from
	 */
	public ReplicationCookie(Path cookieDirectory, URL cookieStatusApiUrl) {
		directory = cookieDirectory;
		cookieStatusAPI = cookieStatusApiUrl;
		data = "";
	}

	/**
	 * Check if this cookie is not empty and used.
	 * 
	 * @return False if it has not been set.
	 */
	public boolean valid() {
		return data != null && !data.isEmpty();
	}

	/**
	 * Get the string representation of the cookie to be set as HTTP header.
	 * 
	 * @return string representation
	 */
	public String toString() {
		return data;
	}

	/**
	 * Read the cookie from a file name cookie.txt in the working directory.
	 */
	public void read() {
		Path cookieFilePath = directory.resolve(Paths.get(COOKIE_FILE_NAME));
		try {
			List<String> lines = Files.readAllLines(cookieFilePath, Charset.forName("US-ASCII"));
			if (lines.size() == 1) {
				data = lines.get(0);
			} else {
				throw new OsmosisRuntimeException("The cookie file " + cookieFilePath.toString()
				+ " must contain exactly one line.");
			}
		} catch (IOException e) {
			throw new OsmosisRuntimeException("Failed to read the cookie file " + cookieFilePath.toString());
		}
	}

	/**
	 * Throw a OsmosisRuntimeException if the cookie isn't accepted by the server any more.
	 *
	 * @throws OsmosisRuntimeException
	 */
	public void throw_if_expired() {
		if (!accepted()) {
			throw new OsmosisRuntimeException("Your cookie is not valid anymore.");
		}
	}

	/**
	 * Check if the cookie is still accepted by the server
	 *
	 * @return acceptance
	 *
	 * @throws OsmosisRuntimeException for empty cookies and IOExceptions
	 */
	public boolean accepted() {
		if (!valid()) {
			throw new OsmosisRuntimeException("Cannot check if the cookie is expired because it is empty.");
		}
		if (cookieStatusAPI == null) {
			return false;
		}
		HttpURLConnection connection;
		try {
			connection = (HttpURLConnection) cookieStatusAPI.openConnection();
			connection.setReadTimeout(15 * 60 * 1000); // timeout 15 minutes
			connection.setConnectTimeout(15 * 60 * 1000); // timeout 15 minutes
			connection.setRequestProperty("User-Agent", "Osmosis/" + OsmosisConstants.VERSION);
			connection.setRequestProperty("Cookie", data);
			// A HTTP HEAD request is sufficient, we don't have to parse the JSON.
			connection.setRequestMethod("HEAD");
			connection.connect();
			return connection.getResponseCode() == HttpURLConnection.HTTP_OK;
		} catch (IOException e) {
			throw new OsmosisRuntimeException("Failed to check if the cookie is still valid.");
		}
	}
}
