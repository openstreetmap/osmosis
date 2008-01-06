// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package com.bretth.osmosis.core.bdb.v0_5.impl;

import com.bretth.osmosis.core.bdb.common.EnvironmentContext;
import com.bretth.osmosis.core.container.v0_5.Dataset;
import com.bretth.osmosis.core.container.v0_5.DatasetReader;


/**
 * Exposes a Berkeley Database as a dataset.
 * 
 * @author Brett Henderson
 */
public class BdbDataset implements Dataset {
	
	private EnvironmentContext envCtx;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param environmentContext
	 *            The database environment to utilise for all operations.
	 */
	public BdbDataset(EnvironmentContext environmentContext) {
		this.envCtx = environmentContext;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public DatasetReader createReader() {
		return new BdbDatasetReader(new TransactionContext(envCtx));
	}
}
