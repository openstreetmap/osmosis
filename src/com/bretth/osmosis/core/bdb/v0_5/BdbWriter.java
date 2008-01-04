// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package com.bretth.osmosis.core.bdb.v0_5;

import java.io.File;

import com.bretth.osmosis.core.bdb.v0_5.impl.DatasetContext;
import com.bretth.osmosis.core.bdb.v0_5.impl.NodeDao;
import com.bretth.osmosis.core.bdb.v0_5.impl.TransactionContext;
import com.bretth.osmosis.core.container.v0_5.EntityContainer;
import com.bretth.osmosis.core.container.v0_5.EntityProcessor;
import com.bretth.osmosis.core.container.v0_5.NodeContainer;
import com.bretth.osmosis.core.container.v0_5.RelationContainer;
import com.bretth.osmosis.core.container.v0_5.WayContainer;
import com.bretth.osmosis.core.task.v0_5.Sink;


/**
 * Receives input data as a stream and builds a java berkeley database
 * containing all of the data.
 * 
 * @author Brett Henderson
 */
public class BdbWriter implements Sink, EntityProcessor {
	private DatasetContext dsCtx;
	private TransactionContext txnCtx;
	private NodeDao nodeDao;
	
	private boolean initialized;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param home
	 *            The directory to store all data files in.
	 */
	public BdbWriter(File home) {
		dsCtx = new DatasetContext(home, true, false);
		
		initialized = false;
	}
	
	
	/**
	 * Opens all database resources for use.
	 */
	private void initialize() {
		txnCtx = dsCtx.createTransaction();
		nodeDao = txnCtx.getNodeDao();
		
		initialized = true;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void process(EntityContainer entityContainer) {
		if (!initialized) {
			initialize();
		}
		entityContainer.process(this);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void process(NodeContainer nodeContainer) {
		nodeDao.putNode(nodeContainer.getEntity());
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void process(WayContainer way) {
		// TODO Auto-generated method stub
		
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void process(RelationContainer relation) {
		// TODO Auto-generated method stub
		
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void complete() {
		if (!initialized) {
			initialize();
		}
		
		txnCtx.complete();
		dsCtx.complete();
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void release() {
		dsCtx.release();
	}
}
