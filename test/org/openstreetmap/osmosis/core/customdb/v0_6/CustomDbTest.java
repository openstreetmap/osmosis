// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package org.openstreetmap.osmosis.core.customdb.v0_6;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

import org.openstreetmap.osmosis.core.Osmosis;
import org.openstreetmap.osmosis.core.OsmosisRuntimeException;

import data.util.DataFileUtilities;


/**
 * Tests for PostgreSQL tasks.
 * 
 * @author Brett Henderson
 */
public class CustomDbTest {
	private DataFileUtilities fileUtils = new DataFileUtilities();
	
	
	private File createTempDirectory() throws IOException {
		File tmpDir;
		
		tmpDir = File.createTempFile("test", null);
		tmpDir.delete();
		
		tmpDir = new File(tmpDir.getAbsolutePath() + File.separator);
		if (!tmpDir.mkdir()) {
			throw new OsmosisRuntimeException("Unable to create directory " + tmpDir + ".");
		}
		
		return tmpDir;
	}
	
	
	private void deleteTempDirectory(File tmpDir) {
		File[] files;
		
		// Delete all files in the directory.
		files = tmpDir.listFiles();
		for (int i = 0; i < files.length; i++) {
			if (!files[i].delete()) {
				throw new OsmosisRuntimeException("Unable to delete file " + files[i] + ".");
			}
		}
		
		// Delete the directory itself.
		if (!tmpDir.delete()) {
			throw new OsmosisRuntimeException("Unable to delete directory " + tmpDir + ".");
		}
	}
	
	
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
		inputFile = fileUtils.getDataFile("v0_6/customdb-snapshot.osm");
		outputFile = File.createTempFile("test", ".osm");
		dataDir = createTempDirectory();
		
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
		fileUtils.compareFiles(inputFile, outputFile);
		
		// Success so delete the temporary files.
		outputFile.delete();
		deleteTempDirectory(dataDir);
	}
}
