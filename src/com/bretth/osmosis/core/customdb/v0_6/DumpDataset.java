// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package com.bretth.osmosis.core.customdb.v0_6;

import com.bretth.osmosis.core.OsmosisRuntimeException;
import com.bretth.osmosis.core.container.v0_6.Dataset;
import com.bretth.osmosis.core.container.v0_6.DatasetReader;
import com.bretth.osmosis.core.container.v0_6.EntityContainer;
import com.bretth.osmosis.core.store.ReleasableIterator;
import com.bretth.osmosis.core.task.v0_6.DatasetSinkSource;
import com.bretth.osmosis.core.task.v0_6.Sink;


/**
 * Reads all data from a dataset.
 * 
 * @author Brett Henderson
 */
public class DumpDataset implements DatasetSinkSource {
	private Sink sink;
	private DatasetReader datasetReader;
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setSink(Sink sink) {
		this.sink = sink;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void process(Dataset dataset) {
		ReleasableIterator<EntityContainer> bboxData;
		
		if (datasetReader != null) {
			throw new OsmosisRuntimeException("process may only be invoked once.");
		}
		
		datasetReader = dataset.createReader();
		
		// Pass all data within the dataset to the sink.
		bboxData = datasetReader.iterate();
		try {
			while (bboxData.hasNext()) {
				sink.process(bboxData.next());
			}
			
			sink.complete();
			
		} finally {
			bboxData.release();
		}
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void release() {
		if (datasetReader != null) {
			datasetReader.release();
		}
	}
}
