// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.set.v0_6;

import java.io.File;
import java.io.IOException;

import org.junit.Test;
import org.openstreetmap.osmosis.core.Osmosis;
import org.openstreetmap.osmosis.testutil.AbstractDataTest;


/**
 * Tests the change simplifier task.
 */
public class ChangeSimplifierTest extends AbstractDataTest {
	
	/**
	 * Tests that a set of changes is simplified correctly.
	 * 
	 * @throws IOException
	 *             if any file operations fail.
	 */
	@Test
	public void test() throws IOException {
		File sourceFile;
		File expectedOutputFile;
		File actualOutputFile;
		
		// Generate files.
		sourceFile = dataUtils.createDataFile("v0_6/simplify-change-in.osc");
		expectedOutputFile = dataUtils.createDataFile("v0_6/simplify-change-out.osc");
		actualOutputFile = dataUtils.newFile();
		
		// Append the two source files into the destination file.
		Osmosis.run(
			new String [] {
				"-q",
				"--read-xml-change-0.6",
				sourceFile.getPath(),
				"--simplify-change-0.6",
				"--write-xml-change-0.6",
				actualOutputFile.getPath()
			}
		);
		
		// Validate that the output file matches the expected result.
		dataUtils.compareFiles(expectedOutputFile, actualOutputFile);
	}
}
