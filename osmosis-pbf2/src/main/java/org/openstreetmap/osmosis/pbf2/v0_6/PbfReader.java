// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.pbf2.v0_6;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.openstreetmap.osmosis.core.OsmosisRuntimeException;
import org.openstreetmap.osmosis.core.task.v0_6.RunnableSource;
import org.openstreetmap.osmosis.core.task.v0_6.Sink;
import org.openstreetmap.osmosis.pbf2.v0_6.impl.PbfDecoder;
import org.openstreetmap.osmosis.pbf2.v0_6.impl.PbfStreamSplitter;

/**
 * An OSM data source reading from a PBF file. The entire contents of the file
 * are read.
 * 
 * @author Brett Henderson
 */
public class PbfReader implements RunnableSource {
	private final InputStream inputStream;
	private final int workers;
	private Sink sink;

	/**
	 * Creates a new instance.
	 * 
	 * @param file
	 *            The file to read.
	 * @param workers
	 *            The number of worker threads for decoding PBF blocks.
	 */
	public PbfReader(File file, int workers) {
		this(streamFor(file), workers);
	}

	/**
	 * Creates a new instance.
	 * 
	 * @param inputStream
	 *            The source stream.
	 * @param workers
	 *            The number of worker threads for decoding PBF blocks.
	 */
	public PbfReader(InputStream inputStream, int workers) {
		this.inputStream = inputStream;
		this.workers = workers;
	}

	@Override
	public void setSink(Sink sink) {
		this.sink = sink;
	}

	@Override
	public void run() {
		PbfStreamSplitter streamSplitter = null;
		ExecutorService executorService = Executors.newFixedThreadPool(workers);

		try {
			sink.initialize(Collections.<String, Object>emptyMap());

			// Create a stream splitter to break the PBF stream into blobs.
			streamSplitter = new PbfStreamSplitter(new DataInputStream(inputStream));

			// Process all blobs of data in the stream using threads from the
			// executor service. We allow the decoder to issue an extra blob
			// than there are workers to ensure there is another blob
			// immediately ready for processing when a worker thread completes.
			// The main thread is responsible for splitting blobs from the
			// request stream, and sending decoded entities to the sink.
			PbfDecoder pbfDecoder = new PbfDecoder(streamSplitter, executorService, workers + 1, sink);
			pbfDecoder.run();

			sink.complete();
		} finally {
			sink.close();

			executorService.shutdownNow();

			if (streamSplitter != null) {
				streamSplitter.close();
			}
		}
	}

	private static InputStream streamFor(File file) {
		try {
			// make "-" an alias for /dev/stdin
			if (file.getName().equals("-")) {
				return System.in;
			}
			return new FileInputStream(file);
		} catch (IOException e) {
			throw new OsmosisRuntimeException("Unable to read PBF file " + file + ".", e);
		}
	}
}
