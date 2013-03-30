// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.xml.common;


/**
 * A utility class for deriving the appropriate compression method based on the
 * file extension.
 * 
 * @author Brett Henderson
 */
public class CompressionMethodDeriver {
	private static final CompressionMethod DEFAULT_COMPRESSION_METHOD = CompressionMethod.None;
	private static final String FILE_SUFFIX_GZIP = ".gz";
	private static final String FILE_SUFFIX_BZIP2 = ".bz2";
	
	
	/**
	 * Determines the appropriate compression method for a file based upon the
	 * file extension.
	 * 
	 * @param fileName
	 *            The name of the file.
	 * @return The compression method.
	 */
	public CompressionMethod deriveCompressionMethod(String fileName) {
		if (fileName.endsWith(FILE_SUFFIX_GZIP)) {
			return CompressionMethod.GZip;
		} else if (fileName.endsWith(FILE_SUFFIX_BZIP2)) {
			return CompressionMethod.BZip2;
		} else {
			return DEFAULT_COMPRESSION_METHOD;
		}
	}
}
