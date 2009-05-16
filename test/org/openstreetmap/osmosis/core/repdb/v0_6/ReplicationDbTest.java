// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.repdb.v0_6;

import java.io.File;
import java.io.IOException;

import org.junit.Test;
import org.openstreetmap.osmosis.core.Osmosis;

import data.util.DataFileUtilities;


/**
 * Tests for Replication DB tasks.
 * 
 * @author Brett Henderson
 */
public class ReplicationDbTest {
	private DataFileUtilities fileUtils = new DataFileUtilities();
	
	
	/**
	 * Tests the replication db queueing functionality.
	 * 
	 * @throws IOException
	 *             if any file operations fail.
	 */
	@Test
	public void testQueueing() throws IOException {
		File authFile;
		File inputFile;
		File outputFile;
		
		authFile = fileUtils.getDataFile("v0_6/repdb-authfile.txt");
		inputFile = fileUtils.getDataFile("v0_6/rep-changeset.osc");
		outputFile = File.createTempFile("test", ".osm");
		
		// Truncate the database.
		Osmosis.run(
			new String [] {
				"-q",
				"--truncate-repdb-0.6",
				"authFile=" + authFile.getPath()
			}
		);
		
		// Create the queue.
		Osmosis.run(
			new String [] {
				"-q",
				"--create-repdb-queue-0.6",
				"authFile=" + authFile.getPath()
			}
		);
		
		// Write data into the database.
		Osmosis.run(
			new String [] {
				"-q",
				"--read-xml-change-0.6",
				"file=" + inputFile.getPath(),
				"--write-repdb-0.6",
				"authFile=" + authFile.getPath()
			}
		);
		
		// Read data from the queue.
		Osmosis.run(
			new String [] {
				"-q",
				"--read-repdb-queue-0.6",
				"authFile=" + authFile.getPath(),
				"--write-xml-change-0.6",
				"file=" + outputFile.getPath()
			}
		);
		
		// Validate that the result file matches the expected result.
		fileUtils.compareFiles(inputFile, outputFile);
		
		// Success so delete the output file.
		outputFile.delete();
	}
}
