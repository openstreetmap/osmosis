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
	 * Creates a new instance.
	 * 
	 * @param keyValueFile
	 *            The file to read key.value tags from.
	 * @throws FileNotFoundException
	 *             in case the specified file does not exist
	 */
	public KeyValueFileReader(final File keyValueFile) throws FileNotFoundException {
		this.reader = new BufferedReader(new FileReader(keyValueFile));
	}


	/**
	 * Reads the file and returns an array of key.value tags.
	 * 
	 * @return an array of key.value tags
	 * @throws IOException
	 *             in case the file could not be read
	 */
	public String[] loadKeyValues() throws IOException {
		List<String> result = new LinkedList<String>();
		try {
			String line;
			while ((line = reader.readLine()) != null) {
				result.add(line);
			}
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
				LOG.log(Level.SEVERE, "Unable to close file reader.", e);
			} finally {
				reader = null;
			}
		}
	}
}
