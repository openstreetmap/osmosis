// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.set.v0_6;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import junit.framework.Assert;

import org.junit.Test;
import org.openstreetmap.osmosis.core.Osmosis;
import org.openstreetmap.osmosis.core.OsmosisRuntimeException;
import org.openstreetmap.osmosis.core.merge.common.ConflictResolutionMethod;
import org.openstreetmap.osmosis.core.misc.v0_6.EmptyReader;
import org.openstreetmap.osmosis.testutil.AbstractDataTest;
import org.openstreetmap.osmosis.testutil.v0_6.RunTaskUtilities;
import org.openstreetmap.osmosis.xml.common.CompressionMethod;
import org.openstreetmap.osmosis.xml.v0_6.XmlReader;

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
		sourceFile = dataUtils.createDataFile("v0_6/merge/merge-in-1.osm");
		expectedOutputFile = dataUtils.createDataFile("v0_6/merge/merge-in-1.osm");
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
		sourceFile = dataUtils.createDataFile("v0_6/merge/merge-in-1.osm");
		expectedOutputFile = dataUtils.createDataFile("v0_6/merge/merge-in-1.osm");
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
		sourceFile1 = dataUtils.createDataFile("v0_6/merge/merge-in-1.osm");
		sourceFile2 = dataUtils.createDataFile("v0_6/merge/merge-in-1.osm");
		expectedOutputFile = dataUtils.createDataFile("v0_6/merge/merge-in-1.osm");
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
		sourceFile1 = dataUtils.createDataFile("v0_6/merge/merge-in-1.osm");
		sourceFile2 = dataUtils.createDataFile("v0_6/merge/merge-in-2-timestamp.osm");
		expectedOutputFile = dataUtils.createDataFile("v0_6/merge/merge-out-timestamp.osm");
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
		sourceFile1 = dataUtils.createDataFile("v0_6/merge/merge-in-1.osm");
		sourceFile2 = dataUtils.createDataFile("v0_6/merge/merge-in-2-version.osm");
		expectedOutputFile = dataUtils.createDataFile("v0_6/merge/merge-out-version.osm");
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
		sourceFile1 = dataUtils.createDataFile("v0_6/merge/merge-in-1.osm");
		sourceFile2 = dataUtils.createDataFile("v0_6/merge/merge-in-2-secondSource.osm");
		expectedOutputFile = dataUtils.createDataFile("v0_6/merge/merge-out-secondSource.osm");
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
		
		expectedOutputFile = dataUtils.createDataFile("v0_6/merge/merge-in-1.osm");

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
		sourceFile1 = dataUtils.createDataFile("v0_6/merge/merge-in-1.osm");
		sourceFile2 = dataUtils.createDataFile("v0_6/merge/merge-in-2-disjunct.osm");
		expectedOutputFile = dataUtils.createDataFile("v0_6/merge/merge-out-disjunct.osm");
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
	
	/**
	 * Tests bad sort order in an input stream (node, way, relations not in
	 * order).
	 * 
	 * @throws Exception
	 *             if something fails
	 */
	@Test
	public void badSortOrderType() throws Exception {
		File sourceFile = dataUtils.createDataFile("v0_6/merge/merge-in-badorder-type.osm");

		mergeAndLookForException(sourceFile, "Pipeline entities are not sorted");
	}
	
	/**
	 * Tests bad sort order in an input stream (ids not sorted).
	 * 
	 * @throws Exception
	 *             if something fails
	 */
	@Test
	public void badSortOrderId() throws Exception {
		File sourceFile = dataUtils.createDataFile("v0_6/merge/merge-in-badorder-id.osm");

		mergeAndLookForException(sourceFile, "Pipeline entities are not sorted");
	}

	/**
	 * Runs a merge and records the exceptions that happened during the merge.
	 * 
	 * The test is considered passed iff at least one exception was thrown and 
	 * the exception message begins with a given string.
	 * 
	 * This method does not use command line parsing because it is impossible 
	 * to check whether the right exception has been thrown. Also, as all 
	 * exceptions are OsmosisRuntimeExceptions, we need to check the message
	 * so the JUnit expected exception facility is no good here. 
	 * 
	 * To add insult to injury, running a merge task involves three worker threads; 
	 * the exception we expect is thrown on one of those worker threads which brings 
	 * the pipeline down. But we want to check for that one source exception
	 * is thrown for the correct reason, otherwise the test becomes very unspecific.
	 * 
	 */
	private void mergeAndLookForException(File sourceFile, String exceptionMessagePrefix) throws Exception {
		final List<Throwable> exceptions = Collections.synchronizedList(new ArrayList<Throwable>());
		
		XmlReader reader = new XmlReader(sourceFile, false, CompressionMethod.None);

		EntityMerger merger = new EntityMerger(
				ConflictResolutionMethod.LatestSource, 1, BoundRemovedAction.Ignore);

		RunTaskUtilities.run(merger, reader, new EmptyReader(), new Thread.UncaughtExceptionHandler() {
			@Override
			public void uncaughtException(Thread t, Throwable e) {
				exceptions.add(e);
			}
		});
		
		// At least one of those exceptions should be a "Pipeline not sorted" one
		boolean sortExceptionFound = false;
		for (Throwable t : exceptions) {
			if (!(t instanceof OsmosisRuntimeException)) {
				Assert.fail("Unexpected exception thrown: " + t);
			}
			
			sortExceptionFound |= t.getMessage().startsWith(exceptionMessagePrefix);
		}
		
		if (!sortExceptionFound) {
			Assert.fail("Expected exception not thrown");
		}
	}
}
