// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.replication.common;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.openstreetmap.osmosis.core.OsmosisRuntimeException;

/**
 * Cookie to be sent with all HTTP requests. The cookie is read from a file.
 *
 * @author Michael Reichert
 */
public class ReplicationCookie {
	private static final String COOKIE_FILE_NAME = "cookie.txt";
	
	private Path directory;
	private String data;

	/**
	 * Create an invalid dummy cookie.
	 */
	public ReplicationCookie() {
		directory = null;
		data = null;
	}

	/**
	 * Creates an empty cookie.
	 *
	 * @param cookieDirectory directory to read the cookie.txt from
	 */
	public ReplicationCookie(Path cookieDirectory) {
		directory = cookieDirectory;
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
}
