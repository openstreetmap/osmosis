// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.set.v0_6;

import java.io.File;
import java.io.IOException;

import org.junit.Test;
import org.openstreetmap.osmosis.core.Osmosis;
import org.openstreetmap.osmosis.testutil.AbstractDataTest;


/**
 * Tests the change to full history convertor task.
 * 
 * @author Brett Henderson
 */
public class ChangeToFullHistoryConvertorTest extends AbstractDataTest {

	/**
	 * Tests appending two change files into a single file.
	 * 
	 * @throws IOException
	 *             if any file operations fail.
	 */
	@Test
	public void testConvert() throws IOException {
		File sourceFile;
		File expectedOutputFile;
		File actualOutputFile;

		// Generate files.
		sourceFile = dataUtils.createDataFile("v0_6/change-to-full-history-in.osc");
		expectedOutputFile = dataUtils.createDataFile("v0_6/change-to-full-history-out.osm");
		actualOutputFile = dataUtils.newFile();

		// Append the two source files into the destination file.
		Osmosis.run(new String[] {"-q", "--read-xml-change-0.6",
				sourceFile.getPath(), "--convert-change-to-full-history-0.6",
				"--write-xml-0.6", actualOutputFile.getPath() });

		// Validate that the output file matches the expected result.
		dataUtils.compareFiles(expectedOutputFile, actualOutputFile);
	}

}
