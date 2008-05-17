// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package com.bretth.osmosis.core.customdb.v0_6;

import java.io.File;

import com.bretth.osmosis.core.customdb.v0_6.impl.DatasetStore;
import com.bretth.osmosis.core.customdb.v0_6.impl.DatasetStoreFileManager;
import com.bretth.osmosis.core.customdb.v0_6.impl.PermanentFileDatasetStoreFileManager;
import com.bretth.osmosis.core.task.v0_6.DatasetSink;
import com.bretth.osmosis.core.task.v0_6.RunnableDatasetSource;


/**
 * An OSM dataset source exposing read-only access to a custom DB database.
 * 
 * @author Brett Henderson
 */
public class ReadDataset implements RunnableDatasetSource {
	
	private DatasetSink datasetSink;
	private DatasetStoreFileManager fileManager;
	private DatasetStore store;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param directory
	 *            The directory to store all data files in.
	 * @param enableWayTileIndex
	 *            If true a tile index is created for ways, otherwise a node-way
	 *            index is used.
	 */
	public ReadDataset(File directory, boolean enableWayTileIndex) {
		fileManager = new PermanentFileDatasetStoreFileManager(directory);
		store = new DatasetStore(fileManager, enableWayTileIndex);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setDatasetSink(DatasetSink datasetSink) {
		this.datasetSink = datasetSink;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void run() {
		try {
			datasetSink.process(store);
			
		} finally {
			datasetSink.release();
			store.release();
		}
	}
}
