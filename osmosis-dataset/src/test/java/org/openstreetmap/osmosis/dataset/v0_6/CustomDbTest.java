// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.dataset.v0_6;

import java.io.File;
import java.io.IOException;

import org.junit.Test;
import org.openstreetmap.osmosis.core.Osmosis;
import org.openstreetmap.osmosis.testutil.AbstractDataTest;


/**
 * Tests for PostgreSQL tasks.
 * 
 * @author Brett Henderson
 */
public class CustomDbTest extends AbstractDataTest {
	
	/**
	 * A basic test loading an osm file into a pgsql database, then dumping it
	 * again and verifying that it is identical.
	 * 
	 * @throws IOException
	 *             if any file operations fail.
	 */
	@Test
	public void testLoadAndDump() throws IOException {
		File inputFile;
		File outputFile;
		File dataDir;
		
		// Generate input files.
		inputFile = dataUtils.createDataFile("v0_6/customdb-snapshot.osm");
		outputFile = File.createTempFile("test", ".osm");
		dataDir = dataUtils.newFolder();
		
		// Load the database with a dataset.
		Osmosis.run(
			new String [] {
				"-q",
				"--read-xml-0.6",
				inputFile.getPath(),
				"--write-customdb-0.6",
				"directory=" + dataDir
			}
		);
		
		// Dump the database to an osm file.
		Osmosis.run(
			new String [] {
				"-q",
				"--read-customdb-0.6",
				"directory=" + dataDir,
				"--dataset-dump-0.6",
				"--tag-sort-0.6",
				"--write-xml-0.6",
				outputFile.getPath()
			}
		);
		
		// Validate that the output file matches the input file.
		dataUtils.compareFiles(inputFile, outputFile);
	}
}
