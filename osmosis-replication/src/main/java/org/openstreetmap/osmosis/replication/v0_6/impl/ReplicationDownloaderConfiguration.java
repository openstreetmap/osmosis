// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.replication.v0_6.impl;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;

import org.openstreetmap.osmosis.core.OsmosisRuntimeException;
import org.openstreetmap.osmosis.core.util.PropertiesPersister;


/**
 * Loads and exposes the configuration properties for downloading replication history.
 * 
 * @author Brett Henderson
 */
public class ReplicationDownloaderConfiguration {
	private static final String KEY_BASE_URL = "baseUrl";
	private static final String KEY_MAX_INTERVAL = "maxInterval";
	private static final String ATTACH_COOKIE = "attachCookie";
	private static final String COOKIE_STATUS_API = "cookieStatusAPI";


	private Properties properties;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param configFile
	 *            The configuration file to read from.
	 */
	public ReplicationDownloaderConfiguration(File configFile) {
		properties = new PropertiesPersister(configFile).load();
	}
	
	
	/**
	 * Returns the URL that change files should be downloaded from.
	 * 
	 * @return The download URL.
	 */
	public URL getBaseUrl() {
		String baseUrl;
		
		baseUrl = properties.getProperty(KEY_BASE_URL);
		
		if (!baseUrl.substring(baseUrl.length() - 1).equals("/")) {
			baseUrl += "/";
		}
		
		try {
			return new URL(baseUrl);
		} catch (MalformedURLException e) {
			throw new OsmosisRuntimeException(
					"Unable to convert URL string (" + baseUrl + ") into a URL.", e);
		}
	}
	
	
	/**
	 * Returns the maximum interval to be downloaded in a single invocation.
	 * 
	 * @return The maximum download interval.
	 */
	public int getMaxInterval() {
		return Integer.parseInt(properties.getProperty(KEY_MAX_INTERVAL)) * 1000;
	}

	/**
	 * Returns whether a cookie stored in cookie.txt should be sent with each request.
	 *
	 * @return If a cookie should be send.
	 */
	public boolean getAttachCookie() {
		return Boolean.parseBoolean(properties.getProperty(ATTACH_COOKIE));
	}

	/**
	 * Returns the API endpoint to use to check if the cookie is expired.
	 *
	 * @return The URL or null if not set.
	 */
	public URL getCookieStatusAPI() {
		String url = properties.getProperty(COOKIE_STATUS_API, null);
		if (url == null) {
			return null;
		}
		try {
			return new URL(url);
		} catch (MalformedURLException e) {
			throw new OsmosisRuntimeException("Cookie status API URL is malformed.");
		}
	}
}
