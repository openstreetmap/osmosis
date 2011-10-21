// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.xml.v0_6;

import java.io.File;
import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.openstreetmap.osmosis.core.Osmosis;
import org.openstreetmap.osmosis.testutil.TestDataUtilities;


/**
 * A simple test verifying the operation of the xml reader and writer tasks.
 * 
 * @author Brett Henderson
 */
public class XmlReaderWriterTest {
	
	private TestDataUtilities dataUtils;
	
	
	/**
	 * Test setup.
	 */
	@Before
	public void setup() {
		dataUtils = new TestDataUtilities();
	}
	
	
	/**
	 * A basic test reading and writing an osm file testing both reader and
	 * writer tasks.
	 * 
	 * @throws IOException
	 *             if any file operations fail.
	 */
	@Test
	public void testSimple() throws IOException {
		File inputFile;
		File outputFile;
		
		// Generate input files.
		inputFile = dataUtils.createDataFile("v0_6/xml-task-tests-v0_6.osm");
		outputFile = dataUtils.createTempFile();
		
		// Run the pipeline.
		Osmosis.run(
			new String [] {
				"-q",
				"--read-xml-0.6",
				inputFile.getPath(),
				"--write-xml-0.6",
				outputFile.getPath()
			}
		);
		
		// Validate that the output file matches the input file.
		dataUtils.compareFiles(inputFile, outputFile);
		
		// Success so delete the temporary files.
		dataUtils.deleteResources();
	}
	
	
	/**
	 * A basic test reading and writing an osm file testing both reader and
	 * writer tasks.
	 * 
	 * @throws IOException
	 *             if any file operations fail.
	 */
	@Test
	public void testSimpleCompressed() throws IOException {
		File uncompressedFile;
		File inputFile;
		File outputFile;
		
		// Generate input files.
		uncompressedFile = dataUtils.createDataFile("v0_6/xml-task-tests-v0_6.osm");
		inputFile = dataUtils.createTempFile("test", ".osm.gz");
		outputFile = dataUtils.createTempFile("test", ".osm.gz");
		dataUtils.compressFile(uncompressedFile, inputFile);
		
		// Run the pipeline.
		Osmosis.run(
			new String [] {
				"-q",
				"--read-xml-0.6",
				inputFile.getPath(),
				"--write-xml-0.6",
				outputFile.getPath()
			}
		);
		
		// Validate that the output file matches the input file.
		dataUtils.compareFiles(inputFile, outputFile);
		
		// Success so delete the temporary files.
		dataUtils.deleteResources();
	}
}
