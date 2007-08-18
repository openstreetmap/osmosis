package com.bretth.osmosis.extract.mysql;

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
	private static final String CONFIG_FILE_PATH = "osmosis-extract-mysql.conf";
	private static final String KEY_HOST = "host";
	
	/*
	# The database host system.
	host=localhost
	# The database instance.
	database=osm
	# The database user.
	user=osm
	# The database password
	password=mypwd
	# The length of an extraction interval in milliseconds (86400000 = 1 day).
	intervalLength=86400000
	*/
	
	private Properties properties;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param baseDirectory
	 *            The root of the extraction file tree.
	 */
	public Configuration(File baseDirectory) {
		File configFile;
		
		configFile = new File(baseDirectory, CONFIG_FILE_PATH);
		
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
}
