// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.tagfilter.v0_6;

import java.io.File;
import java.io.IOException;

import org.junit.Test;
import org.openstreetmap.osmosis.core.Osmosis;
import org.openstreetmap.osmosis.testutil.AbstractDataTest;


/**
 * Tests for the NodeKeyValueFilter class.
 * 
 * @author Raluca Martinescu
 */
public class NodeKeyValueFilterTest extends AbstractDataTest {	

	
	/**
	 * Tests the node key-value filter when allowed value pairs are read from
	 * comma separated list of values.
	 * 
	 * @throws IOException
	 *             if file manipulation fails.
	 */
	@Test
	public final void testNodeKeyValueFilterFromList() throws IOException {
		testNodeKeyValueFilter("keyValueList=box_type.lamp_box,box_type.wall");
	}


	/**
	 * Tests the node key-value filter when allowed value pairs are read from
	 * file.
	 * 
	 * @throws IOException
	 *             if file manipulation fails.
	 */
	@Test
	public final void testNodeKeyValueFilterFromFile() throws IOException {
		File allowedPairs = dataUtils.createDataFile("v0_6/allowed-key-values.txt");
		testNodeKeyValueFilter("keyValueListFile=" + allowedPairs.getPath());
	}


	private void testNodeKeyValueFilter(String keyValueListOption) throws IOException {

		File inputFile = dataUtils.createDataFile("v0_6/node-key-value-filter-snapshot.osm");
		File expectedResultFile = dataUtils.createDataFile("v0_6/node-key-value-filter-expected.osm");
		File outputFile = dataUtils.newFile();
		
		// filter by key-value pairs
		Osmosis.run(
			new String [] {
				"-q",					
				"--read-xml-0.6",
				inputFile.getPath(),
				"--node-key-value",
				keyValueListOption,
				"--write-xml-0.6",
				outputFile.getPath()
			}
		);
					
		// Validate that the output file matches the expected file
		dataUtils.compareFiles(expectedResultFile, outputFile);
	}
}
