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

import org.openstreetmap.osmosis.core.OsmosisRuntimeException;


/**
 * Allows Properties objects to be loaded and stored to file.
 */
public class PropertiesPersister {
	
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
		try (FileInputStream fileInputStream = new FileInputStream(atomicFileCreator.getFile())) {
			Reader reader;
			Properties properties;
			
			reader = new InputStreamReader(new BufferedInputStream(fileInputStream), Charset.forName("UTF-8"));
			
			properties = new Properties();
			properties.load(reader);
			
			return properties;
			
		} catch (IOException e) {
			throw new OsmosisRuntimeException("Unable to read the properties from file " + atomicFileCreator.getFile()
					+ ".", e);
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
		try (FileOutputStream fileOutputStream = new FileOutputStream(atomicFileCreator.getTmpFile())) {
			Writer writer;
			
			writer = new OutputStreamWriter(new BufferedOutputStream(fileOutputStream));
			
			properties.store(writer, null);
			
			writer.close();
			
		} catch (IOException e) {
			throw new OsmosisRuntimeException(
					"Unable to write the properties to temporary file " + atomicFileCreator.getTmpFile() + ".", e);
		}

		atomicFileCreator.renameTmpFileToCurrent();
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
