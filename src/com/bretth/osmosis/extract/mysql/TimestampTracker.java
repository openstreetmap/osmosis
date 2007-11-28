package com.bretth.osmosis.extract.mysql;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.bretth.osmosis.core.OsmosisRuntimeException;


/**
 * Maintains the time that the extraction process has reached. It persists the
 * time across invocations using a file.
 * 
 * @author Brett Henderson
 */
public class TimestampTracker {
	
	private static final Logger log = Logger.getLogger(TimestampTracker.class.getName());
	
	
	private File timestampFile;
	private File tmpFile;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param timestampFile
	 *            The location of the file containing the persisted timestamp.
	 * @param tmpFile
	 *            The location of the temp file to use when updating the
	 *            persisted timestamp to make the update atomic.
	 */
	public TimestampTracker(File timestampFile, File tmpFile) {
		this.timestampFile = timestampFile;
		this.tmpFile = tmpFile;
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
			
			result = new Date(Long.parseLong(reader.readLine()));
			
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
					log.log(Level.WARNING, "Unable to close time file " + timestampFile + ".", e);
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
			
			fileWriter = new FileWriter(tmpFile);
			writer = new BufferedWriter(fileWriter);
			
			writer.write(Long.toString(time.getTime()));
			
			fileWriter.close();
			
			if (!tmpFile.renameTo(timestampFile)) {
				throw new OsmosisRuntimeException("Unable to rename file " + tmpFile + " to " + timestampFile + ".");
			}
			
		} catch (IOException e) {
			throw new OsmosisRuntimeException("Unable to write the time to temporary file " + tmpFile + ".", e);
		} finally {
			if (fileWriter != null) {
				try {
				fileWriter.close();
				} catch (Exception e) {
					log.log(Level.WARNING, "Unable to close temporary time file " + tmpFile + ".", e);
				}
			}
		}
	}
}
