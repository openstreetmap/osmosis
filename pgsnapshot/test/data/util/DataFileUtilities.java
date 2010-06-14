// This software is released into the Public Domain.  See copying.txt for details.
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
	 * Obtains the data file with the specified name. The name is a path relative to the data input
	 * directory.
	 * 
	 * @param systemPropertyName
	 *            The system property to use for getting the file name. If this doesn't exist, the
	 *            dataFileName is used instead.
	 * @param dataFileName
	 *            The name of the data file to be loaded.
	 * @return The file object pointing to the data file.
	 */
	public File getDataFile(String systemPropertyName, String dataFileName) {
		String fileName;
		
		// Get the filename from the system property if it exists.
		fileName = System.getProperty(systemPropertyName);

		if (fileName != null) {
			// Maven environment variable passing hack
			if (!(fileName.startsWith("${"))) {
				return new File(fileName);
			}
		}
		
		// No system property is available so use the provided file name.
		return getDataFile(dataFileName);
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
	
	
	/**
	 * Creates a temporary directory.
	 * 
	 * @return The created directory.
	 * @throws IOException
	 *             if an IO exception occurs.
	 */
	public File createTempDirectory() throws IOException {
		File tmpDir;
		
		tmpDir = File.createTempFile("test", null);
		tmpDir.delete();
		
		tmpDir = new File(tmpDir.getAbsolutePath() + File.separator);
		if (!tmpDir.mkdir()) {
			throw new OsmosisRuntimeException("Unable to create directory " + tmpDir + ".");
		}
		
		return tmpDir;
	}
	
	
	/**
	 * Deletes a temporary directory and its contents.
	 * 
	 * @param tmpDir
	 *            The directory to be deleted.
	 */
	public void deleteTempDirectory(File tmpDir) {
		File[] files;
		
		// Delete all files in the directory.
		files = tmpDir.listFiles();
		for (int i = 0; i < files.length; i++) {
			if (files[i].isDirectory()) {
				deleteTempDirectory(files[i]);
			} else if (!files[i].delete()) {
				throw new OsmosisRuntimeException("Unable to delete file " + files[i] + ".");
			}
		}
		
		// Delete the directory itself.
		if (!tmpDir.delete()) {
			throw new OsmosisRuntimeException("Unable to delete directory " + tmpDir + ".");
		}
	}
}
