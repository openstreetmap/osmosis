// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package com.bretth.osmosis.core.merge.v0_5.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import com.bretth.osmosis.core.OsmosisRuntimeException;


/**
 * Loads and exposes the extraction configuration properties.
 * 
 * @author Brett Henderson
 */
public class DownloaderConfiguration {
	private static final String KEY_BASE_URL = "baseUrl";
	private static final String KEY_CHANGE_FILE_BEGIN_FORMAT = "changeFileBeginFormat";
	private static final String KEY_CHANGE_FILE_END_FORMAT = "changeFileEndFormat";
	private static final String KEY_INTERVAL_LENGTH = "intervalLength";
	
	
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
			
		} catch (IOException e) {
			throw new OsmosisRuntimeException("Unable to load properties from config file " + configFile);
		} finally {
			if (fileInputStream != null) {
				try {
					fileInputStream.close();
				} catch (IOException e) {
					// Ignore errors.
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
		return properties.getProperty(KEY_BASE_URL);
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
}
