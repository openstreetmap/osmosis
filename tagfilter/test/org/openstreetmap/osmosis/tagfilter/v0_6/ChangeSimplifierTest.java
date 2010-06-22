// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.tagfilter.v0_6;

import java.io.File;
import java.io.IOException;

import org.junit.Test;
import org.openstreetmap.osmosis.core.Osmosis;

import data.util.DataFileUtilities;


/**
 * Tests the change simplifier task.
 */
public class ChangeSimplifierTest {
	
	private DataFileUtilities fileUtils = new DataFileUtilities();
	
	
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
		sourceFile = fileUtils.getDataFile("v0_6/simplify-change-in.osc");
		expectedOutputFile = fileUtils.getDataFile("v0_6/simplify-change-out.osc");
		actualOutputFile = File.createTempFile("test", ".osm");
		
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
		fileUtils.compareFiles(expectedOutputFile, actualOutputFile);
		
		// Success so delete the output file.
		actualOutputFile.delete();
	}
}
