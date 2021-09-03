// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.pbf2.v0_6;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;

import com.google.common.util.concurrent.MoreExecutors;
import crosby.binary.Osmformat;
import org.openstreetmap.osmosis.core.OsmosisRuntimeException;
import org.openstreetmap.osmosis.core.container.v0_6.BoundContainer;
import org.openstreetmap.osmosis.core.task.v0_6.RunnableSource;
import org.openstreetmap.osmosis.core.task.v0_6.Sink;
import org.openstreetmap.osmosis.pbf2.v0_6.impl.HeaderBoundReader;
import org.openstreetmap.osmosis.pbf2.v0_6.impl.HeaderMetadataReader;
import org.openstreetmap.osmosis.pbf2.v0_6.impl.HeaderSeeker;
import org.openstreetmap.osmosis.pbf2.v0_6.impl.PbfDecoder;
import org.openstreetmap.osmosis.pbf2.v0_6.impl.StreamSplitter;


/**
 * An OSM data source reading from a PBF file. The entire contents of the file
 * are read.
 * 
 * @author Brett Henderson
 */
public class PbfReader implements RunnableSource {
	private final Supplier<InputStream> supplier;
	private Sink sink;
	private int workers;

	/**
	 * Creates a new instance.
	 * 
	 * @param file
	 *            The file to read.
	 * @param workers
	 *            The number of worker threads for decoding PBF blocks.
	 */
	public PbfReader(final File file, int workers) {
		this(() -> {
			// make "-" an alias for /dev/stdin
			if (file.getName().equals("-")) {
				return System.in;
			}
			try {
				return new FileInputStream(file);
			} catch (IOException e) {
				throw new OsmosisRuntimeException("Unable to read PBF file " + file + ".", e);
			}
		}, workers);
	}

	/**
	 * Creates a new instance.
	 * 
	 * @param supplier
	 *            The inputstream to read.
	 * @param workers
	 *            The number of worker threads for decoding PBF blocks.
	 */
	public PbfReader(Supplier<InputStream> supplier, int workers) {
		this.supplier = supplier;
		this.workers = workers;
	}

	@Override
	public void setSink(Sink sink) {
		this.sink = sink;
	}

	@Override
	public void run() {
		StreamSplitter streamSplitter = null;

		ExecutorService executorService;

		if (workers > 0) {
			executorService = Executors.newFixedThreadPool(workers);
		} else {
			executorService = MoreExecutors.newDirectExecutorService();
		}

		try {
			InputStream inputStream = supplier.get();

			// Create a stream splitter to break the PBF stream into blobs.
			streamSplitter = new StreamSplitter(new DataInputStream(inputStream));

			// Obtain the header block.
			Osmformat.HeaderBlock header = new HeaderSeeker().apply(streamSplitter);

			// Get the pipeline metadata (e.g. do ways include location information) from header.
			Map<String, Object> metadata = new HeaderMetadataReader().apply(header);

			sink.initialize(metadata);

			// Get Bound information from the header.
			BoundContainer bound = new HeaderBoundReader().apply(header);
			sink.process(bound);

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

}
