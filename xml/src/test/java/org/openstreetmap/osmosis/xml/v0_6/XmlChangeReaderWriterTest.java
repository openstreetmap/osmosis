// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.xml.v0_6;

import java.io.File;
import java.io.IOException;

import org.junit.Test;
import org.openstreetmap.osmosis.testutil.AbstractDataTest;
import org.openstreetmap.osmosis.xml.common.CompressionMethod;


/**
 * A simple test verifying the operation of the xml change reader and change
 * writer tasks.
 * 
 * @author Brett Henderson
 */
public class XmlChangeReaderWriterTest extends AbstractDataTest {
	
	/**
	 * A basic test reading and writing an osm file testing both reader and
	 * writer tasks.
	 * 
	 * @throws IOException
	 *             if any file operations fail.
	 */
	@Test
	public void testSimple() throws IOException {
		XmlChangeReader xmlReader;
		XmlChangeWriter xmlWriter;
		File inputFile;
		File outputFile;
		
		inputFile = dataUtils.createDataFile("v0_6/xml-task-tests-v0_6.osc");
		outputFile = dataUtils.newFile();
		
		// Create and connect the xml tasks.
		xmlReader = new XmlChangeReader(inputFile, true, CompressionMethod.None);
		xmlWriter = new XmlChangeWriter(outputFile, CompressionMethod.None);
		xmlReader.setChangeSink(xmlWriter);
		
		// Process the xml.
		xmlReader.run();
		
		// Validate that the output file matches the input file.
		dataUtils.compareFiles(inputFile, outputFile);
	}
}
