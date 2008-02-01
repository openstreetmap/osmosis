// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package com.bretth.osmosis.core.bdb.v0_5;

import java.io.File;

import com.bretth.osmosis.core.bdb.common.EnvironmentContext;
import com.bretth.osmosis.core.bdb.v0_5.impl.BdbDataset;
import com.bretth.osmosis.core.task.v0_5.DatasetSink;
import com.bretth.osmosis.core.task.v0_5.RunnableDatasetSource;


/**
 * An OSM dataset source exposing read-only access to a Berkeley DB database.
 * 
 * @author Brett Henderson
 */
public class BdbReader implements RunnableDatasetSource {
	
	private DatasetSink datasetSink;
	private File home;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param home
	 *            The directory containing all data files.
	 */
	public BdbReader(File home) {
		this.home = home;
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
		EnvironmentContext envCtx;
		
		envCtx = new EnvironmentContext(home, false, true);
		
		try {
			BdbDataset ds;
			
			ds = new BdbDataset(envCtx);
			
			datasetSink.process(ds);
			
		} finally {
			datasetSink.release();
			envCtx.release();
		}
	}
}
