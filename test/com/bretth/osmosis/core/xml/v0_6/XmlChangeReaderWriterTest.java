package com.bretth.osmosis.core.xml.v0_6;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;

import org.junit.Assert;
import org.junit.Test;

import com.bretth.osmosis.core.OsmosisRuntimeException;
import com.bretth.osmosis.core.xml.common.CompressionMethod;


/**
 * A simple test verifying the operation of the xml change reader and change
 * writer tasks.
 * 
 * @author Brett Henderson
 */
public class XmlChangeReaderWriterTest {
	
	/**
	 * Obtains the file for the specified resource.
	 * 
	 * @param resourceName
	 *            The resource to be loaded.
	 * @return The file object pointing to the resource location.
	 */
	private File getFileForResource(String resourceName) {
		URL url;
		File file;
		
		url = getClass().getResource(resourceName);
		if (url == null) {
			throw new OsmosisRuntimeException("Unable to locate resource (" + resourceName + ").");
		}
		file = new File(url.getFile().replaceAll("%20", " "));
		
		return file;
	}
	
	
	/**
	 * Validates the contents of two files for equality.
	 * 
	 * @param file1 The first file.
	 * @param file2 The second file.
	 */
	private void compareFiles(File file1, File file2) throws IOException {
		BufferedInputStream inStream1;
		BufferedInputStream inStream2;
		int byte1;
		int byte2;
		long offset;
		
		inStream1 = new BufferedInputStream(new FileInputStream(file1));
		inStream2 = new BufferedInputStream(new FileInputStream(file2));
		offset = 0;
		do {
			byte1 = inStream1.read();
			byte2 = inStream2.read();
			
			if (byte1 != byte2) {
				Assert.fail("File " + file1 + " and file " + file2 + " are not equal at file offset " + offset + ".");
			}
			
			offset++;
		} while (byte1 >= 0);
		
		inStream2.close();
		inStream1.close();
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
		XmlChangeReader xmlReader;
		XmlChangeWriter xmlWriter;
		File inputFile;
		File outputFile;
		
		inputFile = getFileForResource("/data/input/v0_6/xml-task-tests-v0_6.osc");
		outputFile = File.createTempFile("test", ".osc");
		
		// Create and connect the xml tasks.
		xmlReader = new XmlChangeReader(inputFile, true, CompressionMethod.None);
		xmlWriter = new XmlChangeWriter(outputFile, CompressionMethod.None, false);
		xmlReader.setChangeSink(xmlWriter);
		
		// Process the xml.
		xmlReader.run();
		
		// Validate that the output file matches the input file.
		compareFiles(inputFile, outputFile);
		
		// Success so delete the output file.
		outputFile.delete();
	}
}
