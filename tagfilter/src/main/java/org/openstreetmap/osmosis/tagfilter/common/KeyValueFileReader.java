// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.tagfilter.common;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.openstreetmap.osmosis.core.OsmosisRuntimeException;


/**
 * Reads the content of a file containing "key.value" tags.
 * 
 * <p>
 * The content of the file contains one key.value pair per line. Example:<br/>
 * 
 * <code>
 * railway.tram
 * railway.tram_stop
 * </code>
 * 
 * @author Raluca Martinescu
 */
public class KeyValueFileReader {

	/**
	 * Our logger for debug and error -output.
	 */
	private static final Logger LOG = Logger.getLogger(KeyValueFileReader.class.getName());

	/**
	 * Where we read from.
	 */
	private BufferedReader reader;

	/**
	 * The filename for error-messages.
	 */
	private String fileName;


	/**
	 * Creates a new instance.
	 * 
	 * @param keyValueFile
	 *            The file to read key.value tags from.
	 */
	public KeyValueFileReader(final File keyValueFile) {
		try {
			this.reader = new BufferedReader(new FileReader(keyValueFile));
			this.fileName = keyValueFile.getName();
		} catch (FileNotFoundException ex) {
			throw new OsmosisRuntimeException("Unable to read from key.value file " + fileName + ".", ex);
		}
	}


	/**
	 * Reads the file and returns an array of key.value tags
	 * 
	 * @return an array of key.value tags
	 */
	public String[] loadKeyValues() {
		List<String> result = new LinkedList<String>();
		try {
			String line;
			while ((line = reader.readLine()) != null) {
				result.add(line);
			}
		} catch (IOException ex) {
			throw new OsmosisRuntimeException("Unable to read from key.value file " + fileName + ".", ex);
		} finally {
			cleanup();
		}
		return result.toArray(new String[0]);
	}


	/**
	 * Releases any resources remaining open.
	 */
	private void cleanup() {
		if (reader != null) {
			try {
				reader.close();
			} catch (Exception e) {
				LOG.log(Level.SEVERE, "Unable to close key.value file reader.", e);
			} finally {
				reader = null;
			}
		}
	}
}
