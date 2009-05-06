// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.store;

import java.io.IOException;
import java.io.OutputStream;


/**
 * Tracks the number of bytes written so far to the stream.
 * 
 * @author Brett Henderson
 */
public class OffsetTrackingOutputStream extends OutputStream {
	
	private OutputStream out;
	private long byteCount;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param out
	 *            The destination stream for all written data.
	 */
	public OffsetTrackingOutputStream(OutputStream out) {
		this.out = out;
		
		byteCount = 0;
	}
	
	
	/**
	 * Returns the number of bytes written to this stream so far.
	 * 
	 * @return The current byte count.
	 */
	public long getByteCount() {
		return byteCount;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void write(int b) throws IOException {
		byteCount++;
		
		out.write(b);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void write(byte[] b, int off, int len) throws IOException {
		byteCount += len;
		
		out.write(b, off, len);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void write(byte[] b) throws IOException {
		byteCount += b.length;
		
		out.write(b);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void flush() throws IOException {
		out.flush();
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void close() throws IOException {
		out.close();
	}
}
