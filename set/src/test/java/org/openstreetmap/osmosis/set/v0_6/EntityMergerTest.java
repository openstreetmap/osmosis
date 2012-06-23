// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.set.v0_6;

import java.io.File;

import org.junit.Test;
import org.openstreetmap.osmosis.core.Osmosis;
import org.openstreetmap.osmosis.testutil.AbstractDataTest;

/**
 * Test the --merge task.
 * 
 * @author Igor Podolskiy
 */
public class EntityMergerTest extends AbstractDataTest {
	
	/**
	 * Tests empty + X == X.
	 * 
	 * @throws Exception if something fails
	 */
	@Test
	public void firstEmpty() throws Exception {
		File sourceFile;
		File expectedOutputFile;
		File actualOutputFile;
		
		// Generate files.
		sourceFile = dataUtils.createDataFile("v0_6/merge-in-1.osm");
		expectedOutputFile = dataUtils.createDataFile("v0_6/merge-in-1.osm");
		actualOutputFile = dataUtils.newFile();
		
		// Run the merge.
		Osmosis.run(
			new String [] {
				"-q",
				"--read-xml-0.6", sourceFile.getPath(),
				"--read-empty-0.6",
				"--merge",
				"--write-xml-0.6", actualOutputFile.getPath()
			}
		);
		
		// Validate that the output file matches the expected result.
		dataUtils.compareFiles(expectedOutputFile, actualOutputFile);
	}
	
	/**
	 * Tests X + empty == X.
	 * 
	 * @throws Exception if something fails
	 */
	@Test
	public void secondEmpty() throws Exception {
		File sourceFile;
		File expectedOutputFile;
		File actualOutputFile;
		
		// Generate files.
		sourceFile = dataUtils.createDataFile("v0_6/merge-in-1.osm");
		expectedOutputFile = dataUtils.createDataFile("v0_6/merge-in-1.osm");
		actualOutputFile = dataUtils.newFile();
		
		// Run the merge.
		Osmosis.run(
			new String [] {
				"-q",
				"--read-empty-0.6",
				"--read-xml-0.6", sourceFile.getPath(),
				"--merge",
				"--write-xml-0.6", actualOutputFile.getPath()
			}
		);
		
		// Validate that the output file matches the expected result.
		dataUtils.compareFiles(expectedOutputFile, actualOutputFile);
	}
	
	/**
	 * Tests X + X == X.
	 * 
	 * @throws Exception if something fails
	 */
	@Test
	public void selfMerge() throws Exception {
		File sourceFile1;
		File sourceFile2;
		File expectedOutputFile;
		File actualOutputFile;
		
		// Generate files.
		sourceFile1 = dataUtils.createDataFile("v0_6/merge-in-1.osm");
		sourceFile2 = dataUtils.createDataFile("v0_6/merge-in-1.osm");
		expectedOutputFile = dataUtils.createDataFile("v0_6/merge-in-1.osm");
		actualOutputFile = dataUtils.newFile();
		
		// Run the merge.
		Osmosis.run(
			new String [] {
				"-q",
				"--read-xml-0.6", sourceFile2.getPath(),
				"--read-xml-0.6", sourceFile1.getPath(),
				"--merge",
				"--write-xml-0.6", actualOutputFile.getPath()
			}
		);
		
		// Validate that the output file matches the expected result.
		dataUtils.compareFiles(expectedOutputFile, actualOutputFile);
	}
	
	/**
	 * Tests the timestamp conflict resolution strategy.
	 * 
	 * @throws Exception if something fails
	 */
	@Test
	public void timestampConflictResolution() throws Exception {
		File sourceFile1;
		File sourceFile2;
		File expectedOutputFile;
		File actualOutputFile;
		
		// Generate files.
		sourceFile1 = dataUtils.createDataFile("v0_6/merge-in-1.osm");
		sourceFile2 = dataUtils.createDataFile("v0_6/merge-in-2-timestamp.osm");
		expectedOutputFile = dataUtils.createDataFile("v0_6/merge-out-timestamp.osm");
		actualOutputFile = dataUtils.newFile();
		
		// Run the merge.
		Osmosis.run(
			new String [] {
				"-q",
				"--read-xml-0.6", sourceFile2.getPath(),
				"--read-xml-0.6", sourceFile1.getPath(),
				"--merge",
				"conflictResolutionMethod=timestamp",
				"--write-xml-0.6", actualOutputFile.getPath()
			}
		);
		
		// Validate that the output file matches the expected result.
		dataUtils.compareFiles(expectedOutputFile, actualOutputFile);

		// Timestamp conflict resolution should be commutative.
		
		// Run the merge.
		Osmosis.run(
			new String [] {
				"-q",
				"--read-xml-0.6", sourceFile1.getPath(),
				"--read-xml-0.6", sourceFile2.getPath(),
				"--merge",
				"conflictResolutionMethod=timestamp",
				"--write-xml-0.6", actualOutputFile.getPath()
			}
		);
		
		// Validate that the output file matches the expected result.
		dataUtils.compareFiles(expectedOutputFile, actualOutputFile);

	}
	
	/**
	 * Tests the version conflict resolution strategy.
	 * 
	 * @throws Exception if something fails
	 */
	@Test
	public void versionConflictResolution() throws Exception {
		File sourceFile1;
		File sourceFile2;
		File expectedOutputFile;
		File actualOutputFile;
		
		// Generate files.
		sourceFile1 = dataUtils.createDataFile("v0_6/merge-in-1.osm");
		sourceFile2 = dataUtils.createDataFile("v0_6/merge-in-2-version.osm");
		expectedOutputFile = dataUtils.createDataFile("v0_6/merge-out-version.osm");
		actualOutputFile = dataUtils.newFile();
		
		// Run the merge.
		Osmosis.run(
			new String [] {
				"-q",
				"--read-xml-0.6", sourceFile2.getPath(),
				"--read-xml-0.6", sourceFile1.getPath(),
				"--merge",
				"conflictResolutionMethod=version",
				"--write-xml-0.6", actualOutputFile.getPath()
			}
		);
		
		// Validate that the output file matches the expected result.
		dataUtils.compareFiles(expectedOutputFile, actualOutputFile);

		// Version conflict resolution should be commutative.
		
		// Run the merge.
		Osmosis.run(
			new String [] {
				"-q",
				"--read-xml-0.6", sourceFile1.getPath(),
				"--read-xml-0.6", sourceFile2.getPath(),
				"--merge",
				"conflictResolutionMethod=version",
				"--write-xml-0.6", actualOutputFile.getPath()
			}
		);
		
		// Validate that the output file matches the expected result.
		dataUtils.compareFiles(expectedOutputFile, actualOutputFile);

	}
	
	/**
	 * Tests the version conflict resolution strategy.
	 * 
	 * @throws Exception if something fails
	 */
	@Test
	public void secondSourceConflictResolution() throws Exception {
		File sourceFile1;
		File sourceFile2;
		File expectedOutputFile;
		File actualOutputFile;
		
		// Generate files.
		sourceFile1 = dataUtils.createDataFile("v0_6/merge-in-1.osm");
		sourceFile2 = dataUtils.createDataFile("v0_6/merge-in-2-secondSource.osm");
		expectedOutputFile = dataUtils.createDataFile("v0_6/merge-out-secondSource.osm");
		actualOutputFile = dataUtils.newFile();
		
		// Run the merge.
		Osmosis.run(
			new String [] {
				"-q",
				"--read-xml-0.6", sourceFile2.getPath(),
				"--read-xml-0.6", sourceFile1.getPath(),
				"--merge",
				"conflictResolutionMethod=lastSource",
				"--write-xml-0.6", actualOutputFile.getPath()
			}
		);
		
		// Validate that the output file matches the expected result.
		dataUtils.compareFiles(expectedOutputFile, actualOutputFile);

		// Timestamp conflict resolution is NOT commutative, 
		// but it deserves testing as well.
		// As the mergen-in-2 input does not contain any entities that are not 
		// in the second source, the output should be identical to the first source.
		
		expectedOutputFile = dataUtils.createDataFile("v0_6/merge-in-1.osm");

		// Run the merge.
		Osmosis.run(
			new String [] {
				"-q",
				"--read-xml-0.6", sourceFile1.getPath(),
				"--read-xml-0.6", sourceFile2.getPath(),
				"--merge",
				"conflictResolutionMethod=lastSource",
				"--write-xml-0.6", actualOutputFile.getPath()
			}
		);
		
		// Validate that the output file matches the expected result.
		dataUtils.compareFiles(expectedOutputFile, actualOutputFile);

	}
	
	/**
	 * Tests merging two completely disjunct datasets (no conflicts).
	 * 
	 * @throws Exception if something fails
	 */
	@Test
	public void disjunctDatasets() throws Exception {
		File sourceFile1;
		File sourceFile2;
		File expectedOutputFile;
		File actualOutputFile;
		
		// Generate files.
		sourceFile1 = dataUtils.createDataFile("v0_6/merge-in-1.osm");
		sourceFile2 = dataUtils.createDataFile("v0_6/merge-in-2-disjunct.osm");
		expectedOutputFile = dataUtils.createDataFile("v0_6/merge-out-disjunct.osm");
		actualOutputFile = dataUtils.newFile();
		
		// Run the merge.
		Osmosis.run(
			new String [] {
				"-q",
				"--read-xml-0.6", sourceFile2.getPath(),
				"--read-xml-0.6", sourceFile1.getPath(),
				"--merge",
				"--write-xml-0.6", actualOutputFile.getPath()
			}
		);
		
		// Validate that the output file matches the expected result.
		dataUtils.compareFiles(expectedOutputFile, actualOutputFile);

		// Merging of disjunct datasets should be commutative.
		
		// Run the merge.
		Osmosis.run(
			new String [] {
				"-q",
				"--read-xml-0.6", sourceFile1.getPath(),
				"--read-xml-0.6", sourceFile2.getPath(),
				"--merge",
				"conflictResolutionMethod=version",
				"--write-xml-0.6", actualOutputFile.getPath()
			}
		);
		
		// Validate that the output file matches the expected result.
		dataUtils.compareFiles(expectedOutputFile, actualOutputFile);

	}
}
