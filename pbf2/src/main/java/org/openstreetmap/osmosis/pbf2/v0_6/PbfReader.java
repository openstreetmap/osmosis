// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.pbf2.v0_6;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.openstreetmap.osmosis.core.OsmosisRuntimeException;
import org.openstreetmap.osmosis.core.container.v0_6.EntityContainer;
import org.openstreetmap.osmosis.core.task.v0_6.RunnableSource;
import org.openstreetmap.osmosis.core.task.v0_6.Sink;
import org.openstreetmap.osmosis.pbf2.marshall.Fileformat;
import org.openstreetmap.osmosis.pbf2.marshall.Fileformat.BlobHeader;
import org.openstreetmap.osmosis.pbf2.v0_6.impl.PbfBlobDecoder;



/**
 * An OSM data source reading from a PBF file. The entire contents of the file
 * are read.
 * 
 * @author Brett Henderson
 */
public class PbfReader implements RunnableSource {

	private static Logger log = Logger.getLogger(PbfReader.class.getName());

	private File file;
	private Sink sink;


	/**
	 * Creates a new instance.
	 * 
	 * @param file
	 *            The file to read.
	 */
	public PbfReader(File file) {
		this.file = file;
	}


	@Override
	public void setSink(Sink sink) {
		this.sink = sink;
	}


	private BlobHeader readHeader(int headerLength, DataInputStream dis) throws IOException {
		byte[] headerBuffer = new byte[headerLength];
		dis.readFully(headerBuffer);

		BlobHeader blobHeader = Fileformat.BlobHeader.parseFrom(headerBuffer);

		return blobHeader;
	}


	private byte[] readRawBlob(BlobHeader blobHeader, DataInputStream dis) throws IOException {
		byte[] rawBlob = new byte[blobHeader.getDatasize()];

		dis.readFully(rawBlob);

		return rawBlob;
	}


	private void processBlobs(DataInputStream dis) throws IOException {
		int dataBlockCount = 0;

		while (true) {
			// Read the length of the next header block. This is the only time
			// we should expect to encounter an EOF exception. In all other
			// cases it indicates a corrupt or truncated file.
			int headerLength;
			try {
				headerLength = dis.readInt();
			} catch (EOFException e) {
				return;
			}

			if (log.isLoggable(Level.FINER)) {
				log.finer("Reading header for blob " + dataBlockCount++);
			}
			BlobHeader blobHeader = readHeader(headerLength, dis);

			if (log.isLoggable(Level.FINER)) {
				log.finer("Processing blob of type " + blobHeader.getType() + ".");
			}
			byte[] rawBlob = readRawBlob(blobHeader, dis);

			// Decode the blob data.
			PbfBlobDecoder blobDecoder = new PbfBlobDecoder(blobHeader.getType(), rawBlob);
			blobDecoder.run();
			if (!blobDecoder.isComplete()) {
				throw new OsmosisRuntimeException("The processing of the blob did not complete.");
			}

			// Send all decoded entities to the sink.
			for (EntityContainer entity : blobDecoder.getDecodedEntities()) {
				sink.process(entity);
			}
		}
	}


	@Override
	public void run() {
		InputStream inputStream = null;

		try {
			sink.initialize(Collections.<String, Object>emptyMap());

			// make "-" an alias for /dev/stdin
			if (file.getName().equals("-")) {
				inputStream = System.in;
			} else {
				inputStream = new FileInputStream(file);
			}

			// Process all blobs of data in the stream.
			DataInputStream dis = new DataInputStream(inputStream);
			processBlobs(dis);

			sink.complete();

		} catch (IOException e) {
			throw new OsmosisRuntimeException("Unable to read PBF file " + file + ".", e);
		} finally {
			sink.release();

			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException e) {
					log.log(Level.SEVERE, "Unable to close input stream.", e);
				}
				inputStream = null;
			}
		}
	}
}
