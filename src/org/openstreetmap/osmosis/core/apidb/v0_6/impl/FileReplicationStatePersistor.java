// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.apidb.v0_6.impl;

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
 * A file-based persister for replication state.
 */
public class FileReplicationStatePersistor implements ReplicationStatePersister {
	
	private static final Logger LOG = Logger.getLogger(FileReplicationStatePersistor.class.getName());
	
	
	private File stateFile;
	private File newStateFile;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param stateFile
	 *            The location of the file containing the persisted data.
	 * @param newStateFile
	 *            The location of the temp file to use when updating the
	 *            persisted data to make the update atomic.
	 */
	public FileReplicationStatePersistor(File stateFile, File newStateFile) {
		this.stateFile = stateFile;
		this.newStateFile = newStateFile;
	}


	/**
	 * Renames the new state file to the current file deleting the current file if it exists.
	 */
	private void renameNewFileToCurrent() {
		// Make sure we have a new file.
		if (!newStateFile.exists()) {
			throw new OsmosisRuntimeException("Can't rename non-existent file " + newStateFile + ".");
		}
		
		// Delete the existing file if it exists.
		if (stateFile.exists()) {
			if (!stateFile.delete()) {
				throw new OsmosisRuntimeException("Unable to delete file " + stateFile + ".");
			}
		}
		
		// Rename the new file to the existing file.
		if (!newStateFile.renameTo(stateFile)) {
			throw new OsmosisRuntimeException(
					"Unable to rename file " + newStateFile + " to " + stateFile + ".");
		}
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public ReplicationState loadState() {
		FileInputStream fileInputStream = null;
		
		try {
			Reader reader;
			ReplicationState result;
			Properties properties;
			
			fileInputStream = new FileInputStream(stateFile);
			reader = new InputStreamReader(new BufferedInputStream(fileInputStream), Charset.forName("UTF-8"));
			
			properties = new Properties();
			properties.load(reader);
			
			result = new ReplicationState(properties);
			
			fileInputStream.close();
			fileInputStream = null;
			
			return result;
			
		} catch (IOException e) {
			throw new OsmosisRuntimeException("Unable to read the state from file " + stateFile + ".", e);
		} finally {
			if (fileInputStream != null) {
				try {
					fileInputStream.close();
				} catch (Exception e) {
					LOG.log(Level.WARNING, "Unable to close state file " + stateFile + ".", e);
				}
			}
		}
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void saveState(ReplicationState state) {
		FileOutputStream fileOutputStream = null;
		
		try {
			Writer writer;
			Properties properties;
			
			fileOutputStream = new FileOutputStream(newStateFile);
			writer = new OutputStreamWriter(new BufferedOutputStream(fileOutputStream));
			
			properties = new Properties();
			state.store(properties);
			
			properties.store(writer, null);
			
			writer.close();
			
			renameNewFileToCurrent();
			
		} catch (IOException e) {
			throw new OsmosisRuntimeException(
					"Unable to write the state to temporary file " + newStateFile + ".", e);
		} finally {
			if (fileOutputStream != null) {
				try {
					fileOutputStream.close();
				} catch (Exception e) {
					LOG.log(Level.WARNING, "Unable to close temporary state file " + newStateFile + ".", e);
				}
			}
		}
	}
}
