// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.openstreetmap.osmosis.core.OsmosisRuntimeException;


/**
 * Allows Properties objects to be loaded and stored to file.
 */
public class PropertiesPersister {
	
	private static final Logger LOG = Logger.getLogger(PropertiesPersister.class.getName());
	
	
	private AtomicFileCreator atomicFileCreator;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param propertiesFile
	 *            The location of the file containing the persisted data.
	 */
	public PropertiesPersister(File propertiesFile) {
		atomicFileCreator = new AtomicFileCreator(propertiesFile);
	}
	
	
	/**
	 * Loads the properties from the file.
	 * 
	 * @return The properties.
	 */
	public Properties load() {

		FileInputStream fileInputStream = null;
		
		try {
			Reader reader;
			Properties properties;
			
			fileInputStream = new FileInputStream(atomicFileCreator.getFile());
			reader = new InputStreamReader(new BufferedInputStream(fileInputStream), Charset.forName("UTF-8"));
			
			properties = new Properties();
			properties.load(reader);
			
			fileInputStream.close();
			fileInputStream = null;
			
			return properties;
			
		} catch (IOException e) {
			throw new OsmosisRuntimeException("Unable to read the properties from file " + atomicFileCreator.getFile()
					+ ".", e);
		} finally {
			if (fileInputStream != null) {
				try {
					fileInputStream.close();
				} catch (Exception e) {
					LOG.log(Level.WARNING, "Unable to close properties file " + atomicFileCreator.getFile() + ".", e);
				}
			}
		}
	}
	
	
	/**
	 * Load the properties from a file as a strongly typed Map.
	 * 
	 * @return The properties.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Map<String, String> loadMap() {
		return new HashMap<String, String>((Map) load());
	}
	
	
	/**
	 * Stores the properties to the file overwriting any existing file contents.
	 * 
	 * @param properties
	 *            The properties.
	 */
	public void store(Properties properties) {
		FileOutputStream fileOutputStream = null;
		
		try {
			Writer writer;
			
			fileOutputStream = new FileOutputStream(atomicFileCreator.getTmpFile());
			writer = new OutputStreamWriter(new BufferedOutputStream(fileOutputStream));
			
			properties.store(writer, null);
			
			writer.close();
			
			atomicFileCreator.renameTmpFileToCurrent();
			
		} catch (IOException e) {
			throw new OsmosisRuntimeException(
					"Unable to write the properties to temporary file " + atomicFileCreator.getTmpFile() + ".", e);
		} finally {
			if (fileOutputStream != null) {
				try {
					fileOutputStream.close();
				} catch (Exception e) {
					LOG.log(Level.WARNING, "Unable to close temporary state file " + atomicFileCreator.getTmpFile()
							+ ".", e);
				}
			}
		}
	}
	
	
	/**
	 * Stores the properties to the file overwriting any existing file contents.
	 * 
	 * @param propertiesMap
	 *            The properties.
	 */
	public void store(Map<String, String> propertiesMap) {
		Properties properties = new Properties();
		properties.putAll(propertiesMap);
		store(properties);
	}


	/**
	 * Checks if the properties file exists.
	 * 
	 * @return True if a file exists, false otherwise.
	 */
	public boolean exists() {
		return atomicFileCreator.exists();
	}
}
