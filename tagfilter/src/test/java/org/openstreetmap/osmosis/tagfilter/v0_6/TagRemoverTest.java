// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.tagfilter.v0_6;

import java.io.File;
import java.io.IOException;

import org.junit.Test;
import org.openstreetmap.osmosis.core.Osmosis;
import org.openstreetmap.osmosis.testutil.AbstractDataTest;


/**
 * Tests for the tag remover task.
 * 
 * @author Brett Henderson
 */
public class TagRemoverTest extends AbstractDataTest {
	
	/**
	 * Tests tag removal functionality using full key names.
	 * 
	 * @throws IOException
	 *             if file manipulation fails.
	 */
	@Test
	public void testKey() throws IOException {
		File inputFile;
		File outputFile;
		File expectedResultFile;
		
		inputFile = dataUtils.createDataFile("v0_6/tag-remove-snapshot.osm");
		expectedResultFile = dataUtils.createDataFile("v0_6/tag-remove-expected.osm");
		outputFile = dataUtils.newFile();
		
		// Remove all created_by tags.
		Osmosis.run(
			new String [] {
				"-q",
				"--read-xml-0.6",
				inputFile.getPath(),
				"--remove-tags-0.6",
				"keys=created_by",
				"--write-xml-0.6",
				outputFile.getPath()
			}
		);
		
		// Validate that the output file matches the input file.
		dataUtils.compareFiles(expectedResultFile, outputFile);
	}
	
	
	/**
	 * Tests tag removal functionality using full key names.
	 * 
	 * @throws IOException
	 *             if file manipulation fails.
	 */
	@Test
	public void testKeyPrefix() throws IOException {
		File inputFile;
		File outputFile;
		File expectedResultFile;
		
		inputFile = dataUtils.createDataFile("v0_6/tag-remove-snapshot.osm");
		expectedResultFile = dataUtils.createDataFile("v0_6/tag-remove-expected.osm");
		outputFile = dataUtils.newFile();
		
		// Remove all created_by tags.
		Osmosis.run(
			new String [] {
				"-q",
				"--read-xml-0.6",
				inputFile.getPath(),
				"--remove-tags-0.6",
				"keyPrefixes=crea",
				"--write-xml-0.6",
				outputFile.getPath()
			}
		);
		
		// Validate that the output file matches the input file.
		dataUtils.compareFiles(expectedResultFile, outputFile);
	}
}
