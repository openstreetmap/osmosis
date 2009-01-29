// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package data.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.zip.GZIPOutputStream;

import org.junit.Assert;

import org.openstreetmap.osmosis.core.OsmosisRuntimeException;


/**
 * Provides re-usable functionality for utilising data files within junit tests.
 * 
 * @author Brett Henderson
 */
public class DataFileUtilities {
	
	/**
	 * Obtains the data file with the specified name. The name is a path
	 * relative to the data input directory.
	 * 
	 * @param dataFileName
	 *            The name of the data file to be loaded.
	 * @return The file object pointing to the data file.
	 */
	public File getDataFile(String dataFileName) {
		URL url;
		File file;
		
		url = getClass().getResource("/data/input/" + dataFileName);
		if (url == null) {
			throw new OsmosisRuntimeException("The data file (" + dataFileName + ") could not be found.");
		}
		file = new File(url.getFile().replaceAll("%20", " "));
		
		return file;
	}
	
	
	/**
	 * Validates the contents of two files for equality.
	 * 
	 * @param file1
	 *            The first file.
	 * @param file2
	 *            The second file.
	 * @throws IOException
	 *             if an exception occurs.
	 */
	public void compareFiles(File file1, File file2) throws IOException {
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
	 * @param inputFile
	 *            The uncompressed input file.
	 * @param outputFile
	 *            The compressed output file to generate.
	 * @throws IOException
	 *             if an exception occurs.
	 */
	public void compressFile(File inputFile, File outputFile) throws IOException {
		BufferedInputStream inStream;
		BufferedOutputStream outStream;
		byte[] buffer;
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
}
