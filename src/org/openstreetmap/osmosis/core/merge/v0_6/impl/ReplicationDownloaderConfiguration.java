// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.merge.v0_6.impl;

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
	private static final String KEY_INTERVAL_LENGTH = "intervalLength";
	private static final String KEY_MAX_DOWNLOAD_COUNT = "maxDownloadCount";
	
	
	private Properties properties;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param configFile
	 *            The configuration file to read from.
	 */
	public ReplicationDownloaderConfiguration(File configFile) {
		properties = new PropertiesPersister(configFile, null).load();
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
	 * Returns the duration of each changeset interval.
	 * 
	 * @return The interval length in milliseconds.
	 */
	public int getIntervalLength() {
		return Integer.parseInt(properties.getProperty(KEY_INTERVAL_LENGTH)) * 1000;
	}
	
	
	/**
	 * Returns the maximum number of files to download in a single invocation.
	 * 
	 * @return The maximum download count.
	 */
	public int getMaxDownloadCount() {
		return Integer.parseInt(properties.getProperty(KEY_MAX_DOWNLOAD_COUNT));
	}
}
