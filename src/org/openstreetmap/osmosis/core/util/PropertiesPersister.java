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
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.openstreetmap.osmosis.core.OsmosisRuntimeException;


/**
 * Allows Properties objects to be loaded and stored to file.
 */
public class PropertiesPersister {
	
	private static final Logger LOG = Logger.getLogger(PropertiesPersister.class.getName());
	
	
	private File propertiesFile;
	private File tmpPropertiesFile;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param propertiesFile
	 *            The location of the file containing the persisted data.
	 * @param tmpPropertiesFile
	 *            The location of the temp file to use when updating the
	 *            persisted data to make the update atomic.
	 */
	public PropertiesPersister(File propertiesFile, File tmpPropertiesFile) {
		this.propertiesFile = propertiesFile;
		this.tmpPropertiesFile = tmpPropertiesFile;
	}


	/**
	 * Renames the new state file to the current file deleting the current file if it exists.
	 */
	private void renameNewFileToCurrent() {
		// Make sure we have a new file.
		if (!tmpPropertiesFile.exists()) {
			throw new OsmosisRuntimeException("Can't rename non-existent file " + tmpPropertiesFile + ".");
		}
		
		// Delete the existing file if it exists.
		if (propertiesFile.exists()) {
			if (!propertiesFile.delete()) {
				throw new OsmosisRuntimeException("Unable to delete file " + propertiesFile + ".");
			}
		}
		
		// Rename the new file to the existing file.
		if (!tmpPropertiesFile.renameTo(propertiesFile)) {
			throw new OsmosisRuntimeException(
					"Unable to rename file " + tmpPropertiesFile + " to " + propertiesFile + ".");
		}
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
			
			fileInputStream = new FileInputStream(propertiesFile);
			reader = new InputStreamReader(new BufferedInputStream(fileInputStream), Charset.forName("UTF-8"));
			
			properties = new Properties();
			properties.load(reader);
			
			fileInputStream.close();
			fileInputStream = null;
			
			return properties;
			
		} catch (IOException e) {
			throw new OsmosisRuntimeException("Unable to read the properties from file " + propertiesFile + ".", e);
		} finally {
			if (fileInputStream != null) {
				try {
					fileInputStream.close();
				} catch (Exception e) {
					LOG.log(Level.WARNING, "Unable to close properties file " + propertiesFile + ".", e);
				}
			}
		}
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
			
			fileOutputStream = new FileOutputStream(tmpPropertiesFile);
			writer = new OutputStreamWriter(new BufferedOutputStream(fileOutputStream));
			
			properties.store(writer, null);
			
			writer.close();
			
			renameNewFileToCurrent();
			
		} catch (IOException e) {
			throw new OsmosisRuntimeException(
					"Unable to write the properties to temporary file " + tmpPropertiesFile + ".", e);
		} finally {
			if (fileOutputStream != null) {
				try {
					fileOutputStream.close();
				} catch (Exception e) {
					LOG.log(Level.WARNING, "Unable to close temporary state file " + tmpPropertiesFile + ".", e);
				}
			}
		}
	}


	/**
	 * Checks if either one of the main or temporary files currently exists.
	 * 
	 * @return True if a file exists, false otherwise.
	 */
	public boolean exists() {
		// We're checking both files because there is a small window where only the new file exists
		// after the main state file is deleted before the new file being renamed.
		// See http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4017593 for more details.
		return propertiesFile.exists() || tmpPropertiesFile.exists();
	}
}
