package org.openstreetmap.osmosis.core.migrate;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

import org.openstreetmap.osmosis.core.Osmosis;

import data.util.DataFileUtilities;


/**
 * Tests for migration tasks.
 * 
 * @author Brett Henderson
 */
public class MigrateTest {
	private DataFileUtilities fileUtils = new DataFileUtilities();
	
	
	/**
	 * A basic test migration of a 0.5 file to 0.6 format.
	 * 
	 * @throws IOException
	 *             if any file operations fail.
	 */
	@Test
	public void testMigrate() throws IOException {
		File inputFile;
		File expectedOutputFile;
		File outputFile;
		
		// Generate files.
		inputFile = fileUtils.getDataFile("v0_5/migration-input.osm");
		expectedOutputFile = fileUtils.getDataFile("v0_6/migration-expected.osm");
		outputFile = File.createTempFile("test", ".osm");
		
		// Migrate from 0.5 to 0.6 format.
		Osmosis.run(
			new String [] {
				"-q",
				"--read-xml-0.5",
				inputFile.getPath(),
				"--migrate",
				"--write-xml-0.6",
				outputFile.getPath()
			}
		);
		
		// Validate that the output file matches the input file.
		fileUtils.compareFiles(expectedOutputFile, outputFile);
		
		// Success so delete the output file.
		outputFile.delete();
	}
	
	
	/**
	 * A basic test migration of a 0.5 change file to 0.6 format.
	 * 
	 * @throws IOException
	 *             if any file operations fail.
	 */
	@Test
	public void testMigrateChange() throws IOException {
		File inputFile;
		File expectedOutputFile;
		File outputFile;
		
		// Generate files.
		inputFile = fileUtils.getDataFile("v0_5/migration-input.osc");
		expectedOutputFile = fileUtils.getDataFile("v0_6/migration-expected.osc");
		outputFile = File.createTempFile("test", ".osc");
		
		// Migrate from 0.5 to 0.6 format.
		Osmosis.run(
			new String [] {
				"-q",
				"--read-xml-change-0.5",
				inputFile.getPath(),
				"--migrate-change",
				"--write-xml-change-0.6",
				outputFile.getPath()
			}
		);
		
		// Validate that the output file matches the input file.
		fileUtils.compareFiles(expectedOutputFile, outputFile);
		
		// Success so delete the output file.
		outputFile.delete();
	}
}
