// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.tagfilter.common;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.osmosis.testutil.AbstractDataTest;


/**
 * Tests for the KeyValueFileReader class.
 * 
 * @author Raluca Martinescu
 */
public class KeyValueFileReaderTest extends AbstractDataTest {

	/**
	 * Tests that in case the file does not exist, the KeyValueFileReader
	 * constructor throws an exception.
	 * 
	 * @throws FileNotFoundException
	 *             when the test class encounters the non-existent file.
	 */
	@Test(expected = FileNotFoundException.class)
	public final void testFileNotFound() throws FileNotFoundException {
		File file = new File("non_existing_file.txt");
		new KeyValueFileReader(file);
	}


	/**
	 * Tests the reading of key-value pairs from the file.
	 * 
	 * @throws IOException
	 *             if the allowed-key-values file cannot be found.
	 */
	@Test
	public final void testLoadKeyValues() throws IOException {
		File file = dataUtils.createDataFile("v0_6/allowed-key-values.txt");
		String[] expected = {"box_type.lamp_box", "box_type.wall"};
		String[] actual = new KeyValueFileReader(file).loadKeyValues();
		Assert.assertArrayEquals(expected, actual);
	}
}
