// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package com.bretth.osmosis.core.bdb.v0_5.impl;

import java.util.Iterator;

import com.bretth.osmosis.core.container.v0_5.DatasetReader;
import com.bretth.osmosis.core.container.v0_5.EntityContainer;
import com.bretth.osmosis.core.domain.v0_5.Node;
import com.bretth.osmosis.core.domain.v0_5.Relation;
import com.bretth.osmosis.core.domain.v0_5.Way;


/**
 * Exposes a Berkeley Database as a dataset reader.
 * 
 * @author Brett Henderson
 */
public class BdbDatasetReader implements DatasetReader {
	
	private TransactionContext txnCtx;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param transactionContext
	 *            Provides access to the underlying data store.
	 */
	public BdbDatasetReader(TransactionContext transactionContext) {
		this.txnCtx = transactionContext;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Node getNode(long id) {
		return txnCtx.getNodeDao().getNode(id);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Relation getRelation(long id) {
		return txnCtx.getRelationDao().getRelation(id);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Way getWay(long id) {
		return txnCtx.getWayDao().getWay(id);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Iterator<EntityContainer> iterate() {
		// TODO Auto-generated method stub
		return null;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Iterator<EntityContainer> iterateBoundingBox(double left,
			double right, double top, double bottom, boolean completeWays) {
		// TODO Auto-generated method stub
		return null;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void release() {
		txnCtx.release();
	}
}
