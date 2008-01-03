package com.bretth.osmosis.core.customdb.v0_5;

import com.bretth.osmosis.core.container.v0_5.EntityContainer;
import com.bretth.osmosis.core.customdb.v0_5.impl.DatasetStore;
import com.bretth.osmosis.core.customdb.v0_5.impl.DatasetStoreFileManager;
import com.bretth.osmosis.core.customdb.v0_5.impl.TempFileDatasetStoreFileManager;
import com.bretth.osmosis.core.task.v0_5.DatasetSink;
import com.bretth.osmosis.core.task.v0_5.SinkDatasetSource;


/**
 * Receives input data as a stream and builds a dataset containing all of the
 * data. The dataset is then passed to the downstream task.
 * 
 * @author Brett Henderson
 */
public class WriteDatasetAndForward implements SinkDatasetSource {
	
	private DatasetSink datasetSink;
	private DatasetStoreFileManager fileManager;
	private DatasetStore store;
	
	
	/**
	 * Creates a new instance.
	 */
	public WriteDatasetAndForward() {
		fileManager = new TempFileDatasetStoreFileManager();
		store = new DatasetStore(fileManager);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void setDatasetSink(DatasetSink datasetSink) {
		this.datasetSink = datasetSink;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void process(EntityContainer entityContainer) {
		store.process(entityContainer);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void complete() {
		store.complete();
		
		datasetSink.process(store);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void release() {
		datasetSink.release();
		
		// We must release the store last because downstream tasks must be able
		// to release store readers first.
		store.release();
		
		// We must release the file manager after the store to ensure all open
		// files are closed.
		fileManager.release();
	}
}
