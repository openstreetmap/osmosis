// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.merge.v0_5.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.openstreetmap.osmosis.core.OsmosisRuntimeException;


/**
 * Loads and exposes the extraction configuration properties.
 * 
 * @author Brett Henderson
 */
public class DownloaderConfiguration {
	private static final Logger LOG = Logger.getLogger(DownloaderConfiguration.class.getName());
	
	private static final String KEY_BASE_URL = "baseUrl";
	private static final String KEY_CHANGE_FILE_BEGIN_FORMAT = "changeFileBeginFormat";
	private static final String KEY_CHANGE_FILE_END_FORMAT = "changeFileEndFormat";
	private static final String KEY_INTERVAL_LENGTH = "intervalLength";
	private static final String KEY_MAX_DOWNLOAD_COUNT = "maxDownloadCount";
	
	
	private Properties properties;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param configFile
	 *            The configuration file to read from.
	 */
	public DownloaderConfiguration(File configFile) {
		properties = loadProperties(configFile);
	}
	
	
	private Properties loadProperties(File configFile) {
		FileInputStream fileInputStream = null;
		
		properties = new Properties();
		
		try {
			fileInputStream = new FileInputStream(configFile);
			
			properties.load(fileInputStream);
			
			fileInputStream.close();
			fileInputStream = null;
			
		} catch (IOException e) {
			throw new OsmosisRuntimeException("Unable to load properties from config file " + configFile);
		} finally {
			if (fileInputStream != null) {
				try {
					fileInputStream.close();
				} catch (IOException e) {
					// We are already in an error condition so log and continue.
					LOG.log(Level.WARNING, "Unable to close property file.", e);
				}
			}
		}
		
		return properties;
	}
	
	
	/**
	 * Returns the URL that change files should be downloaded from.
	 * 
	 * @return The download URL.
	 */
	public String getBaseUrl() {
		String baseUrl;
		
		baseUrl = properties.getProperty(KEY_BASE_URL);
		
		if (!baseUrl.substring(baseUrl.length() - 1).equals("/")) {
			baseUrl += "/";
		}
		
		return baseUrl;
	}
	
	
	/**
	 * Returns the begin time portion of the changeset filename.
	 * 
	 * @return The format.
	 */
	public String getChangeFileBeginFormat() {
		return properties.getProperty(KEY_CHANGE_FILE_BEGIN_FORMAT);
	}
	
	
	/**
	 * Returns the end time portion of the changeset filename.
	 * 
	 * @return The format.
	 */
	public String getChangeFileEndFormat() {
		return properties.getProperty(KEY_CHANGE_FILE_END_FORMAT);
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
