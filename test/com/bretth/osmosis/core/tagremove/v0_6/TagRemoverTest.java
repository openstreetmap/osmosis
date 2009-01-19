// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package com.bretth.osmosis.core.tagremove.v0_6;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

import com.bretth.osmosis.core.Osmosis;

import data.util.DataFileUtilities;


/**
 * Tests for the tag remover task.
 * 
 * @author Brett Henderson
 */
public class TagRemoverTest {
	private DataFileUtilities fileUtils = new DataFileUtilities();
	
	/**
	 * Tests tag removal functionality using full key names.
	 */
	@Test
	public void testKey() throws IOException {
		File inputFile;
		File outputFile;
		File expectedResultFile;
		
		inputFile = fileUtils.getDataFile("v0_6/tag-remove-snapshot.osm");
		expectedResultFile = fileUtils.getDataFile("v0_6/tag-remove-expected.osm");
		outputFile = File.createTempFile("test", ".osm");
		
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
		fileUtils.compareFiles(expectedResultFile, outputFile);
		
		// Success so delete the output file.
		outputFile.delete();
	}
	
	
	/**
	 * Tests tag removal functionality using full key names.
	 */
	@Test
	public void testKeyPrefix() throws IOException {
		File inputFile;
		File outputFile;
		File expectedResultFile;
		
		inputFile = fileUtils.getDataFile("v0_6/tag-remove-snapshot.osm");
		expectedResultFile = fileUtils.getDataFile("v0_6/tag-remove-expected.osm");
		outputFile = File.createTempFile("test", ".osm");
		
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
		fileUtils.compareFiles(expectedResultFile, outputFile);
		
		// Success so delete the output file.
		outputFile.delete();
	}
}
