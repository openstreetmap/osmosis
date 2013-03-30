// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.tagtransform.v0_6;

import java.io.File;
import java.io.IOException;

import org.junit.Test;
import org.openstreetmap.osmosis.core.Osmosis;
import org.openstreetmap.osmosis.testutil.AbstractDataTest;


/**
 * Tests the tag transform functionality.
 * 
 * @author Brett Henderson
 */
public class TagTransformTest extends AbstractDataTest {

	/**
	 * Tests transforming all tags in a single OSM file.
	 * 
	 * @throws IOException
	 *             if any file operations fail.
	 */
	@Test
	public void testTransform() throws IOException {
		File sourceFile;
		File translationFile;
		File expectedOutputFile;
		File actualOutputFile;

		// Generate files.
		sourceFile = dataUtils.createDataFile("v0_6/test-in.osm");
		translationFile = dataUtils.createDataFile("v0_6/translation.xml");
		expectedOutputFile = dataUtils.createDataFile("v0_6/test-out.osm");
		actualOutputFile = dataUtils.newFile();

		// Append the two source files into the destination file.
		Osmosis.run(
			new String[] {
				"-q",
				"--read-xml-0.6",
				sourceFile.getPath(),
				"--tag-transform-0.6",
				"file=" + translationFile,
				"--tag-sort-0.6",
				"--write-xml-0.6",
				actualOutputFile.getPath()
			}
		);

		// Validate that the output file matches the expected result.
		dataUtils.compareFiles(expectedOutputFile, actualOutputFile);
	}
}
