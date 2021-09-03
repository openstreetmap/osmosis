// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.pbf2.v0_6.impl;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import crosby.binary.Fileformat;
import org.openstreetmap.osmosis.core.OsmosisRuntimeException;
import org.openstreetmap.osmosis.core.lifecycle.Closeable;

/**
 * Parses a PBF data stream and extracts the raw data of each blob in sequence
 * until the end of the stream is reached.
 * 
 * @author Brett Henderson
 */
public class StreamSplitter implements Iterator<RawBlob>, Closeable {

	private static Logger log = Logger.getLogger(StreamSplitter.class.getName());

	private DataInputStream dis;
	private int dataBlockCount;
	private boolean eof;
	private RawBlob nextBlob;


	/**
	 * Creates a new instance.
	 * 
	 * @param pbfStream
	 *            The PBF data stream to be parsed.
	 */
	public StreamSplitter(DataInputStream pbfStream) {
		dis = pbfStream;
		dataBlockCount = 0;
		eof = false;
	}


	private Fileformat.BlobHeader readHeader(int headerLength) throws IOException {
		byte[] headerBuffer = new byte[headerLength];
		dis.readFully(headerBuffer);

		return Fileformat.BlobHeader.parseFrom(headerBuffer);
	}


	private byte[] readRawBlob(Fileformat.BlobHeader blobHeader) throws IOException {
		byte[] rawBlob = new byte[blobHeader.getDatasize()];

		dis.readFully(rawBlob);

		return rawBlob;
	}


	private void getNextBlob() {
		try {
			// Read the length of the next header block. This is the only time
			// we should expect to encounter an EOF exception. In all other
			// cases it indicates a corrupt or truncated file.
			int headerLength;
			try {
				headerLength = dis.readInt();
			} catch (EOFException e) {
				eof = true;
				return;
			}

			if (log.isLoggable(Level.FINER)) {
				log.finer("Reading header for blob " + dataBlockCount++);
			}
			Fileformat.BlobHeader blobHeader = readHeader(headerLength);

			if (log.isLoggable(Level.FINER)) {
				log.finer("Processing blob of type " + blobHeader.getType() + ".");
			}
			byte[] blobData = readRawBlob(blobHeader);

			nextBlob = new RawBlob(blobHeader.getType(), blobData);

		} catch (IOException e) {
			throw new OsmosisRuntimeException("Unable to get next blob from PBF stream.", e);
		}
	}


	@Override
	public boolean hasNext() {
		if (nextBlob == null && !eof) {
			getNextBlob();
		}

		return nextBlob != null;
	}


	@Override
	public RawBlob next() {
		RawBlob result = nextBlob;
		nextBlob = null;

		return result;
	}


	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}


	@Override
	public void close() {
		if (dis != null) {
			try {
				dis.close();
			} catch (IOException e) {
				log.log(Level.SEVERE, "Unable to close PBF stream.", e);
			}
		}
		dis = null;
	}
}
