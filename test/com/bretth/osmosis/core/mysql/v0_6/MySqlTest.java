package com.bretth.osmosis.core.mysql.v0_6;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

import com.bretth.osmosis.core.Osmosis;

import data.util.DataFileUtilities;


/**
 * Tests for MySQL tasks.
 * @author Brett Henderson
 */
public class MySqlTest {
	private DataFileUtilities fileUtils = new DataFileUtilities();
	
	
	/**
	 * A basic test loading an osm file into a mysql database, then dumping it
	 * again and verifying that it is identical.
	 * 
	 * @throws IOException
	 *             if any file operations fail.
	 */
	@Test
	public void testLoadAndDump() throws IOException {
		File authFile;
		File inputFile;
		File outputFile;
		
		// Generate input files.
		authFile = fileUtils.getDataFile("v0_6/mysql-authfile.txt");
		inputFile = fileUtils.getDataFile("v0_6/db-snapshot.osm");
		outputFile = File.createTempFile("test", ".osm");
		
		// Remove all existing data from the database.
		Osmosis.run(
			new String [] {
				"-q",
				"--truncate-mysql-0.6",
				"authFile=" + authFile.getPath()
			}
		);
		
		// Load the database with a dataset.
		Osmosis.run(
			new String [] {
				"-q",
				"--read-xml-0.6",
				inputFile.getPath(),
				"--write-mysql-0.6",
				"authFile=" + authFile.getPath()
			}
		);
		
		// Dump the database to an osm file.
		Osmosis.run(
			new String [] {
				"-q",
				"--read-mysql-0.6",
				"authFile=" + authFile.getPath(),
				"--dataset-dump-0.6",
				"--write-xml-0.6",
				outputFile.getPath()
			}
		);
		
		// Validate that the output file matches the input file.
		fileUtils.compareFiles(inputFile, outputFile);
		
		// Success so delete the output file.
		outputFile.delete();
	}
	
	
	/**
	 * A test loading an osm file into a mysql database, then applying a
	 * changeset, then dumping it again and verifying the output is as expected.
	 * 
	 * @throws IOException
	 *             if any file operations fail.
	 */
	@Test
	public void testChangeset() throws IOException {
		File authFile;
		File snapshotFile;
		File changesetFile;
		File expectedResultFile;
		File actualResultFile;
		
		// Generate input files.
		authFile = fileUtils.getDataFile("v0_6/mysql-authfile.txt");
		snapshotFile = fileUtils.getDataFile("v0_6/db-snapshot.osm");
		changesetFile = fileUtils.getDataFile("v0_6/db-changeset.osc");
		expectedResultFile = fileUtils.getDataFile("v0_6/db-expected.osm");
		actualResultFile = File.createTempFile("test", ".osm");
		
		// Remove all existing data from the database.
		Osmosis.run(
			new String [] {
				"-q",
				"--truncate-mysql-0.6",
				"authFile=" + authFile.getPath()
			}
		);
		
		// Load the database with the snapshot file.
		Osmosis.run(
			new String [] {
				"-q",
				"--read-xml-0.6",
				snapshotFile.getPath(),
				"--write-mysql-0.6",
				"authFile=" + authFile.getPath()
			}
		);
		
		// Apply the changeset file to the database.
		Osmosis.run(
			new String [] {
				"-q",
				"--read-xml-change-0.6",
				changesetFile.getPath(),
				"--write-mysql-change-0.6",
				"authFile=" + authFile.getPath()
			}
		);
		
		// Dump the database to an osm file.
		Osmosis.run(
			new String [] {
				"-q",
				"--read-mysql-0.6",
				"authFile=" + authFile.getPath(),
				"--dataset-dump-0.6",
				"--write-xml-0.6",
				actualResultFile.getPath()
			}
		);
		
		// Validate that the dumped file matches the expected result.
		fileUtils.compareFiles(expectedResultFile, actualResultFile);
		
		// Success so delete the output file.
		actualResultFile.delete();
	}
}
