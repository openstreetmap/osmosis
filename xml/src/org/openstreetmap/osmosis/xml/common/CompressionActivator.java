// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.xml.common;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorOutputStream;

import org.openstreetmap.osmosis.core.OsmosisRuntimeException;
import org.openstreetmap.osmosis.core.util.MultiMemberGZIPInputStream;


/**
 * A utility class for layering compression streams on top of underlying byte
 * streams based upon a specified compression algorithm.
 * 
 * @author Brett Henderson
 */
public class CompressionActivator {
	
	private CompressionMethod compressionMethod;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param compressionMethod
	 *            The compression method to employ.
	 */
	public CompressionActivator(CompressionMethod compressionMethod) {
		this.compressionMethod = compressionMethod;
	}
	
	
	/**
	 * Wraps a compression stream around the destination stream based upon the
	 * requested compression method. If this method returns successfully, the
	 * input destination stream does not require closing after use because it
	 * will be closed when the returned output stream is closed.
	 * 
	 * @param destinationStream
	 *            The destination stream for receiving compressed data.
	 * @return A stream for writing compressed data to the destination stream.
	 */
	public OutputStream createCompressionOutputStream(OutputStream destinationStream) {
		try {
			if (CompressionMethod.None.equals(compressionMethod)) {
				return destinationStream;
			}
			
			if (CompressionMethod.GZip.equals(compressionMethod)) {
				return new GZIPOutputStream(destinationStream);
			}
			
			if (CompressionMethod.BZip2.equals(compressionMethod)) {
				return new BZip2CompressorOutputStream(destinationStream);
			}
			
			throw new OsmosisRuntimeException("Compression method " + compressionMethod + " is not recognized.");
			
		} catch (IOException e) {
			throw new OsmosisRuntimeException(
					"Unable to instantiate a " + compressionMethod + " compression stream.", e);
		}
	}
	
	
	/**
	 * Wraps a compression stream around the source stream based upon the
	 * requested compression method. If this method returns successfully, the
	 * input source stream does not require closing after use because it will be
	 * closed when the returned input stream is closed.
	 * 
	 * @param sourceStream
	 *            The source stream for providing compressed data.
	 * @return A stream for writing compressed data to the destination stream.
	 */
	public InputStream createCompressionInputStream(InputStream sourceStream) {
		try {
			if (CompressionMethod.None.equals(compressionMethod)) {
				return sourceStream;
			}
			
			if (CompressionMethod.GZip.equals(compressionMethod)) {
				return new MultiMemberGZIPInputStream(sourceStream);
			}
			
			if (CompressionMethod.BZip2.equals(compressionMethod)) {
				return new BZip2CompressorInputStream(sourceStream);
			}
			
			throw new OsmosisRuntimeException("Compression method " + compressionMethod + " is not recognized.");
			
		} catch (IOException e) {
			throw new OsmosisRuntimeException(
					"Unable to instantiate a " + compressionMethod + " compression stream.", e);
		}
	}
}
