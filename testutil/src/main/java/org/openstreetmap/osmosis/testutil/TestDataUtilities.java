// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.testutil;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.zip.GZIPOutputStream;

import org.junit.Assert;
import org.junit.rules.TemporaryFolder;
import org.openstreetmap.osmosis.core.OsmosisConstants;
import org.openstreetmap.osmosis.core.OsmosisRuntimeException;


/**
 * Provides re-usable functionality for utilising data files within junit tests.
 * 
 * @author Brett Henderson
 */
public class TestDataUtilities extends TemporaryFolder {

	private static final Charset UTF8 = Charset.forName("UTF-8");


	/**
	 * Obtains the data file with the specified name. The name is a path
	 * relative to the data input directory. The returned file will have any
	 * occurrences of %VERSION% replaced with the current version.
	 * 
	 * @param dataFileName
	 *            The name of the data file to be loaded.
	 * @return The file object pointing to the data file.
	 */
	public File createDataFile(String dataFileName) {
		try {
			BufferedReader dataReader;
			BufferedWriter dataWriter;
			File tmpFile;
			String line;

			// Open the data template file.
			dataReader = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream(
					"/data/template/" + dataFileName), UTF8));

			// Create a temporary file and open it.
			tmpFile = newFile();
			dataWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(tmpFile), UTF8));

			// Copy all data into the temp file replacing the version string.
			while ((line = dataReader.readLine()) != null) {
				line = line.replace("%VERSION%", OsmosisConstants.VERSION);
				dataWriter.write(line);
				dataWriter.newLine();
			}

			dataReader.close();
			dataWriter.close();

			return tmpFile;
		} catch (IOException e) {
			throw new OsmosisRuntimeException("Unable to build test data file " + dataFileName, e);
		}
	}


	/**
	 * Obtains the data file with the specified name. The name is a path
	 * relative to the data input directory.
	 * 
	 * @param systemPropertyName
	 *            The system property to use for getting the file name. If this
	 *            doesn't exist, the dataFileName is used instead.
	 * @param dataFileName
	 *            The name of the data file to be loaded.
	 * @return The file object pointing to the data file.
	 */
	public File createDataFile(String systemPropertyName, String dataFileName) {
		String fileName;

		// Get the filename from the system property if it exists.
		fileName = System.getProperty(systemPropertyName);

		if (fileName != null) {
			return new File(fileName);
		}

		// No system property is available so use the provided file name.
		return createDataFile(dataFileName);
	}
	
	
	private void copyFiles(File from, File to) throws IOException {
		byte[] buffer;
		int bytesRead;
		BufferedInputStream isFrom;
		BufferedOutputStream osTo;
		
		buffer = new byte[4096];
		
		isFrom = new BufferedInputStream(new FileInputStream(from));
		osTo = new BufferedOutputStream(new FileOutputStream(to));
		
		while ((bytesRead = isFrom.read(buffer)) >= 0) {
			osTo.write(buffer, 0, bytesRead);
		}
		
		isFrom.close();
		osTo.close();
	}
	
	
	private void handleInequalFiles(File file1, File file2, long failureoffset) throws IOException {
		File file1Copy;
		File file2Copy;
		
		// We must create copies of the files because the originals will be
		// cleaned up at the completion of the test.
		file1Copy = File.createTempFile("junit", null);
		file2Copy = File.createTempFile("junit", null);
		
		copyFiles(file1, file1Copy);
		copyFiles(file2, file2Copy);
		
		Assert.fail("File " + file1Copy + " and file " + file2Copy + " are not equal at file offset " + failureoffset
				+ ".");
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
				handleInequalFiles(file1, file2, offset);
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
