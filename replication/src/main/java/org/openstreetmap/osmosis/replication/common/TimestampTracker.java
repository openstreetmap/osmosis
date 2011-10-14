// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.replication.common;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.openstreetmap.osmosis.core.OsmosisRuntimeException;
import org.openstreetmap.osmosis.core.time.DateFormatter;
import org.openstreetmap.osmosis.core.time.DateParser;


/**
 * Maintains the time that the extraction process has reached. It persists the
 * time across invocations using a file.
 * 
 * @author Brett Henderson
 */
public class TimestampTracker {
	
	private static final Logger LOG = Logger.getLogger(TimestampTracker.class.getName());
	
	
	private File timestampFile;
	private File newTimestampFile;
	private DateParser dateParser;
	private DateFormatter dateFormatter;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param timestampFile
	 *            The location of the file containing the persisted timestamp.
	 * @param newTimestampFile
	 *            The location of the temp file to use when updating the
	 *            persisted timestamp to make the update atomic.
	 */
	public TimestampTracker(File timestampFile, File newTimestampFile) {
		this.timestampFile = timestampFile;
		this.newTimestampFile = newTimestampFile;
		
		dateParser = new DateParser();
		dateFormatter = new DateFormatter();
	}
	
	
	/**
	 * Renames the new timestamp file to the current file deleting the current
	 * file if it exists.
	 */
	private void renameNewFileToCurrent() {
		// Make sure we have a new timestamp file.
		if (!newTimestampFile.exists()) {
			throw new OsmosisRuntimeException("Can't rename non-existent file " + newTimestampFile + ".");
		}
		
		// Delete the existing timestamp file if it exists.
		if (timestampFile.exists()) {
			if (!timestampFile.delete()) {
				throw new OsmosisRuntimeException("Unable to delete file " + timestampFile + ".");
			}
		}
		
		// Rename the new file to the existing file.
		if (!newTimestampFile.renameTo(timestampFile)) {
			throw new OsmosisRuntimeException(
					"Unable to rename file " + newTimestampFile + " to " + timestampFile + ".");
		}
	}
	
	
	/**
	 * Retrieve the current time.
	 * 
	 * @return The time.
	 */
	public Date getTime() {
		FileReader fileReader = null;
		
		try {
			BufferedReader reader;
			Date result;
			
			fileReader = new FileReader(timestampFile);
			reader = new BufferedReader(fileReader);
			
			result = dateParser.parse(reader.readLine());
			
			fileReader.close();
			fileReader = null;
			
			return result;
			
		} catch (IOException e) {
			throw new OsmosisRuntimeException("Unable to read the time from file " + timestampFile + ".", e);
		} finally {
			if (fileReader != null) {
				try {
				fileReader.close();
				} catch (Exception e) {
					LOG.log(Level.WARNING, "Unable to close time file " + timestampFile + ".", e);
				}
			}
		}
	}
	
	
	/**
	 * Update the stored time.
	 * 
	 * @param time
	 *            The time to set.
	 */
	public void setTime(Date time) {
		FileWriter fileWriter = null;
		
		try {
			BufferedWriter writer;
			
			fileWriter = new FileWriter(newTimestampFile);
			writer = new BufferedWriter(fileWriter);
			
			writer.write(dateFormatter.format(time));
			
			writer.close();
			
			renameNewFileToCurrent();
			
		} catch (IOException e) {
			throw new OsmosisRuntimeException(
					"Unable to write the time to temporary file " + newTimestampFile + ".", e);
		} finally {
			if (fileWriter != null) {
				try {
					fileWriter.close();
				} catch (Exception e) {
					LOG.log(Level.WARNING, "Unable to close temporary time file " + newTimestampFile + ".", e);
				}
			}
		}
	}
}
