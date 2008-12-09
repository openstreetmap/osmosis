// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package com.bretth.osmosis.extract.mysql.common;

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
public class Configuration {
	private static final String KEY_HOST = "host";
	private static final String KEY_DATABASE = "database";
	private static final String KEY_USER = "user";
	private static final String KEY_PASSWORD = "password";
	private static final String KEY_INTERVAL_LENGTH = "intervalLength";
	private static final String KEY_LAG_LENGTH = "lagLength";
	private static final String KEY_CHANGE_FILE_BEGIN_FORMAT = "changeFileBeginFormat";
	private static final String KEY_CHANGE_FILE_END_FORMAT = "changeFileEndFormat";
	private static final String KEY_ENABLE_PROD_ENCODING_HACK = "enableProdEncodingHack";
	private static final String KEY_READ_FULL_HISTORY = "readFullHistory";
	
	
	private Properties properties;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param configFile
	 *            The configuration file to read from.
	 */
	public Configuration(File configFile) {
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
	 * Returns the database host.
	 * 
	 * @return The database host.
	 */
	public String getHost() {
		return properties.getProperty(KEY_HOST);
	}
	
	
	/**
	 * Returns the database instance.
	 * 
	 * @return The database instance.
	 */
	public String getDatabase() {
		return properties.getProperty(KEY_DATABASE);
	}
	
	
	/**
	 * Returns the database user.
	 * 
	 * @return The database user.
	 */
	public String getUser() {
		return properties.getProperty(KEY_USER);
	}
	
	
	/**
	 * Returns the database password.
	 * 
	 * @return The database password.
	 */
	public String getPassword() {
		return properties.getProperty(KEY_PASSWORD);
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
	 * Returns the amount of time the extraction process lags the current time
	 * to allow the database to stabilise to ensure consistent queries.
	 * 
	 * @return The lag length in milliseconds.
	 */
	public int getLagLength() {
		return Integer.parseInt(properties.getProperty(KEY_LAG_LENGTH)) * 1000;
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
	 * Returns the production encoding hack flag.
	 * 
	 * @return The production encoding hack flag.
	 */
	public boolean getEnableProductionEncodingHack() {
		return Boolean.valueOf(properties.getProperty(KEY_ENABLE_PROD_ENCODING_HACK));
	}
	
	
	/**
	 * Returns the full history flag.
	 * 
	 * @return The full history flag.
	 */
	public boolean getReadFullHistory() {
		return Boolean.valueOf(properties.getProperty(KEY_READ_FULL_HISTORY));
	}
}
