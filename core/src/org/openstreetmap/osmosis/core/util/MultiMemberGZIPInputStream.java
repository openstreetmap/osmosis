// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.util;

import java.io.InputStream;
import java.io.PushbackInputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;


/**
 * This class was copied directly from the workaround class provided in
 * http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4691425.
 */
public class MultiMemberGZIPInputStream extends GZIPInputStream {

	/**
	 * Creates a new instance.
	 * 
	 * @param in
	 *            The input stream.
	 * @param size
	 *            The buffer size.
	 * @throws IOException
	 *             if an IO exception occurs.
	 */
	public MultiMemberGZIPInputStream(InputStream in, int size)
			throws IOException {
		// Wrap the stream in a PushbackInputStream...
		super(new PushbackInputStream(in, size), size);
		this.size = size;
	}

	/**
	 * Creates a new instance.
	 * 
	 * @param in
	 *            The input stream.
	 * @throws IOException
	 *             if an IO exception occurs.
	 */
	public MultiMemberGZIPInputStream(InputStream in) throws IOException {
		// Wrap the stream in a PushbackInputStream...
		super(new PushbackInputStream(in, 1024));
		this.size = -1;
	}

	private MultiMemberGZIPInputStream(MultiMemberGZIPInputStream parent)
			throws IOException {
		super(parent.in);
		this.size = -1;
		if (parent.parent == null) {
			this.parent = parent;
		} else {
			this.parent = parent.parent;
		}
		this.parent.child = this;
	}

	private MultiMemberGZIPInputStream(MultiMemberGZIPInputStream parent,
			int size) throws IOException {
		super(parent.in, size);
		this.size = size;
		if (parent.parent == null) {
			this.parent = parent;
		} else {
			this.parent = parent.parent;
		}
		this.parent.child = this;
	}

	private MultiMemberGZIPInputStream parent;
	private MultiMemberGZIPInputStream child;
	private int size;
	private boolean eos;

	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public int read(byte[] inputBuffer, int inputBufferOffset,
			int inputBufferLen) throws IOException {

		if (eos) {
			return -1;
		}
		if (this.child != null) {
			return this.child.read(inputBuffer, inputBufferOffset,
					inputBufferLen);
		}

		int charsRead = super.read(inputBuffer, inputBufferOffset,
				inputBufferLen);
		if (charsRead == -1) {
			// Push any remaining buffered data back onto the stream
			// If the stream is then not empty, use it to construct
			// a new instance of this class and delegate this and any
			// future calls to it...
			int n = inf.getRemaining() - 8;
			if (n > 0) {
				// More than 8 bytes remaining in deflater
				// First 8 are gzip trailer. Add the rest to
				// any un-read data...
				((PushbackInputStream) this.in).unread(buf, len - n, n);
			} else {
				// Nothing in the buffer. We need to know whether or not
				// there is unread data available in the underlying stream
				// since the base class will not handle an empty file.
				// Read a byte to see if there is data and if so,
				// push it back onto the stream...
				byte[] b = new byte[1];
				int ret = in.read(b, 0, 1);
				if (ret == -1) {
					eos = true;
					return -1;
				} else {
					((PushbackInputStream) this.in).unread(b, 0, 1);
				}
			}

			MultiMemberGZIPInputStream tmpChild;
			if (this.size == -1) {
				tmpChild = new MultiMemberGZIPInputStream(this);
			} else {
				tmpChild = new MultiMemberGZIPInputStream(this, this.size);
			}
			return tmpChild.read(inputBuffer, inputBufferOffset, inputBufferLen);
		} else {
			return charsRead;
		}
	}

}
