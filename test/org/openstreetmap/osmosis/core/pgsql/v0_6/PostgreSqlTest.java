// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.pgsql.v0_6;

import java.io.File;
import java.io.IOException;

import org.junit.Test;
import org.openstreetmap.osmosis.core.Osmosis;

import data.util.DataFileUtilities;


/**
 * Tests for PostgreSQL tasks.
 * 
 * @author Brett Henderson
 */
public class PostgreSqlTest {
	private DataFileUtilities fileUtils = new DataFileUtilities();
	
	
	private File getAuthFile() {
		return fileUtils.getDataFile("db.pgsql.authfile", "v0_6/pgsql-authfile.txt");
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
		File authFile;
		File inputFile;
		File outputFile;
		
		// Generate input files.
		authFile = getAuthFile();
		inputFile = fileUtils.getDataFile("v0_6/db-snapshot.osm");
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
				"--tag-sort-0.6",
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
	 * A basic test loading an osm file into a pgsql database using the COPY
	 * file approach, then dumping it again and verifying that it is identical.
	 * 
	 * @throws IOException
	 *             if any file operations fail.
	 */
	@Test
	public void testFastLoadAndDump() throws IOException {
		File authFile;
		File inputFile;
		File outputFile;
		
		// Generate input files.
		authFile = getAuthFile();
		inputFile = fileUtils.getDataFile("v0_6/db-snapshot.osm");
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
				"--fast-write-pgsql-0.6",
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
				"--tag-sort-0.6",
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
	 * A test loading an osm file into a pgsql database, then applying a
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
		authFile = getAuthFile();
		snapshotFile = fileUtils.getDataFile("v0_6/db-snapshot.osm");
		changesetFile = fileUtils.getDataFile("v0_6/db-changeset.osc");
		expectedResultFile = fileUtils.getDataFile("v0_6/db-changeset-expected.osm");
		actualResultFile = File.createTempFile("test", ".osm");
		
		// Remove all existing data from the database.
		Osmosis.run(
			new String [] {
				"-q",
				"--truncate-pgsql-0.6",
				"authFile=" + authFile.getPath()
			}
		);
		
		// Load the database with the snapshot file.
		Osmosis.run(
			new String [] {
				"-q",
				"--read-xml-0.6",
				snapshotFile.getPath(),
				"--write-pgsql-0.6",
				"authFile=" + authFile.getPath()
			}
		);
		
		// Apply the changeset file to the database.
		Osmosis.run(
			new String [] {
				"-q",
				"--read-xml-change-0.6",
				changesetFile.getPath(),
				"--write-pgsql-change-0.6",
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
				"--tag-sort-0.6",
				"--write-xml-0.6",
				actualResultFile.getPath()
			}
		);
		
		// Validate that the dumped file matches the expected result.
		fileUtils.compareFiles(expectedResultFile, actualResultFile);
		
		// Success so delete the output file.
		actualResultFile.delete();
	}


	/**
	 * A test loading an osm file into a pgsql database, then making some modifications via the
	 * dataset api, then dumping it again and verifying the output is as expected.
	 * 
	 * @throws IOException
	 *             if any file operations fail.
	 */
	@Test
	public void testDataset() throws IOException {
		File authFile;
		File snapshotFile;
		File expectedResultFile;
		File actualResultFile;
		
		// Generate input files.
		authFile = getAuthFile();
		snapshotFile = fileUtils.getDataFile("v0_6/db-snapshot.osm");
		expectedResultFile = fileUtils.getDataFile("v0_6/db-dataset-expected.osm");
		actualResultFile = File.createTempFile("test", ".osm");
		
		// Remove all existing data from the database.
		Osmosis.run(
			new String [] {
				"-q",
				"--truncate-pgsql-0.6",
				"authFile=" + authFile.getPath()
			}
		);
		
		// Load the database with the snapshot file.
		Osmosis.run(
			new String [] {
				"-q",
				"--read-xml-0.6",
				snapshotFile.getPath(),
				"--write-pgsql-0.6",
				"authFile=" + authFile.getPath()
			}
		);
		
		// Invoke the dataset driver task task to manipulate the database.
		Osmosis.run(
			new String [] {
				"-q",
				"-p",
				DatasetDriverPlugin.class.getName(),
				"--read-pgsql-0.6",
				"authFile=" + authFile.getPath(),
				"--drive-dataset"
			}
		);
		
		// Dump the database to an osm file.
		Osmosis.run(
			new String [] {
				"-q",
				"--read-pgsql-0.6",
				"authFile=" + authFile.getPath(),
				"--dataset-dump-0.6",
				"--tag-sort-0.6",
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
