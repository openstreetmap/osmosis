// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package com.bretth.osmosis.core.customdb.v0_5;

import java.io.File;

import com.bretth.osmosis.core.container.v0_5.EntityContainer;
import com.bretth.osmosis.core.customdb.v0_5.impl.DatasetStore;
import com.bretth.osmosis.core.customdb.v0_5.impl.DatasetStoreFileManager;
import com.bretth.osmosis.core.customdb.v0_5.impl.PermanentFileDatasetStoreFileManager;
import com.bretth.osmosis.core.task.v0_5.Sink;


/**
 * Receives input data as a stream and builds a dataset containing all of the
 * data.
 * 
 * @author Brett Henderson
 */
public class WriteDataset implements Sink {
	
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
	public WriteDataset(File directory, boolean enableWayTileIndex) {
		fileManager = new PermanentFileDatasetStoreFileManager(directory);
		store = new DatasetStore(fileManager, enableWayTileIndex);
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
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void release() {
		// We must release the store last because downstream tasks must be able
		// to release store readers first.
		store.release();
		
		// We must release the file manager after the store to ensure all open
		// files are closed.
		fileManager.release();
	}
}
