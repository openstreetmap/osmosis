package com.bretth.osmosis.core.xml.v0_6;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.zip.GZIPOutputStream;

import org.junit.Assert;
import org.junit.Test;

import com.bretth.osmosis.core.Osmosis;
import com.bretth.osmosis.core.xml.common.CompressionMethod;

/**
 * A simple test verifying the operation of the xml reader and writer tasks.
 * 
 * @author Brett Henderson
 */
public class XmlReaderWriterTest {
	
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
	 * Compresses the contents of a file into a new compressed file.
	 * 
	 * @param inputFile The uncompressed input file.
	 * @param file2 The compressed output file to generate.
	 */
	private void compressFile(File inputFile, File outputFile) throws IOException {
		BufferedInputStream inStream;
		BufferedOutputStream outStream;
		byte buffer[];
		int bytesRead;
		
		inStream = new BufferedInputStream(new FileInputStream(inputFile));
		outStream = new BufferedOutputStream(new GZIPOutputStream(new FileOutputStream(outputFile)));
		
		buffer = new byte[4096];
		
		do {
			bytesRead = inStream.read(buffer);
			if (bytesRead > 0) {
				outStream.write(buffer, 0, bytesRead);
			}
		} while (bytesRead >= 0);
		
		outStream.close();
		inStream.close();
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
		XmlReader xmlReader;
		XmlWriter xmlWriter;
		File inputFile;
		File outputFile;
		
		inputFile = getFileForResource("/data/input/v0_6/xml-task-tests-v0_6.osm");
		outputFile = File.createTempFile("test", ".osm");
		
		// Create and connect the xml tasks.
		xmlReader = new XmlReader(inputFile, true, CompressionMethod.None);
		xmlWriter = new XmlWriter(outputFile, CompressionMethod.None, false);
		xmlReader.setSink(xmlWriter);
		
		// Process the xml.
		xmlReader.run();
		
		// Validate that the output file matches the input file.
		compareFiles(inputFile, outputFile);
		
		// Success so delete the output file.
		outputFile.delete();
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
		
		uncompressedFile = getFileForResource("/data/input/v0_6/xml-task-tests-v0_6.osm");
		inputFile = File.createTempFile("test", ".osm.gz");
		outputFile = File.createTempFile("test", ".osm.gz");
		
		compressFile(uncompressedFile, inputFile);
		
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
		compareFiles(inputFile, outputFile);
		
		// Success so delete the temp files.
		outputFile.delete();
		inputFile.delete();
	}
}
