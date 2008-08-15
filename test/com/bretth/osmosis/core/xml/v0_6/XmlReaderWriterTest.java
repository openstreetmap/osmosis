package com.bretth.osmosis.core.xml.v0_6;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

import com.bretth.osmosis.core.Osmosis;

import data.util.DataFileUtilities;


/**
 * A simple test verifying the operation of the xml reader and writer tasks.
 * 
 * @author Brett Henderson
 */
public class XmlReaderWriterTest {
	
	private DataFileUtilities fileUtils = new DataFileUtilities();
	
	
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
		inputFile = fileUtils.getDataFile("v0_6/xml-task-tests-v0_6.osm");
		outputFile = File.createTempFile("test", ".osm");
		
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
		fileUtils.compareFiles(inputFile, outputFile);
		
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
		
		// Generate input files.
		uncompressedFile = fileUtils.getDataFile("v0_6/xml-task-tests-v0_6.osm");
		inputFile = File.createTempFile("test", ".osm.gz");
		outputFile = File.createTempFile("test", ".osm.gz");
		fileUtils.compressFile(uncompressedFile, inputFile);
		
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
		fileUtils.compareFiles(inputFile, outputFile);
		
		// Success so delete the temp files.
		outputFile.delete();
		inputFile.delete();
	}
}
