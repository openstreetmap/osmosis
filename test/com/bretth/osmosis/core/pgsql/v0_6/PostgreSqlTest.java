package com.bretth.osmosis.core.pgsql.v0_6;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

import com.bretth.osmosis.core.Osmosis;

import data.util.DataFileUtilities;


/**
 * Tests for PostgreSQL tasks.
 * @author Brett Henderson
 */
public class PostgreSqlTest {
	private DataFileUtilities fileUtils = new DataFileUtilities();
	
	
	/**
	 * A basic test loading an osm file into a pgsql database, then dumping it
	 * again and verifying that it is identical.
	 * 
	 * @throws IOException
	 *             if any file operations fail.
	 */
	@Test
	public void testSimple() throws IOException {
		File authFile;
		File inputFile;
		File outputFile;
		
		// Generate input files.
		authFile = fileUtils.getDataFile("v0_6/pgsql-authfile.txt");
		inputFile = fileUtils.getDataFile("v0_6/pgsql-task-tests-v0_6.osm");
		outputFile = File.createTempFile("test", ".osm");
		
		// Remove all existing data from the database.
		Osmosis.run(
			new String [] {
				"-q",
				"--truncate-pgsql-0.6",
				"authFile=" + authFile.getPath()
			}
		);
		
		// Load the database with a dataset.
		Osmosis.run(
			new String [] {
				"-q",
				"--read-xml-0.6",
				inputFile.getPath(),
				"--write-pgsql-0.6",
				"authFile=" + authFile.getPath()
			}
		);
		
		// Dump the database to an osm file.
		Osmosis.run(
			new String [] {
				"-q",
				"--read-pgsql-0.6",
				"authFile=" + authFile.getPath(),
				"--dataset-dump-0.6",
				"--write-xml-0.6",
				inputFile.getPath()
			}
		);
		
		// Validate that the output file matches the input file.
		fileUtils.compareFiles(inputFile, outputFile);
		
		// Success so delete the output file.
		outputFile.delete();
	}
}
