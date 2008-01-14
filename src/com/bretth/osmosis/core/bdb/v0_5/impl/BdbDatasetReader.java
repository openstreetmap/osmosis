// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package com.bretth.osmosis.core.bdb.v0_5.impl;

import java.util.ArrayList;
import java.util.List;

import com.bretth.osmosis.core.container.v0_5.EntityContainer;
import com.bretth.osmosis.core.container.v0_5.NodeContainer;
import com.bretth.osmosis.core.container.v0_5.NodeContainerIterator;
import com.bretth.osmosis.core.container.v0_5.RelationContainer;
import com.bretth.osmosis.core.container.v0_5.RelationContainerIterator;
import com.bretth.osmosis.core.container.v0_5.WayContainer;
import com.bretth.osmosis.core.container.v0_5.WayContainerIterator;
import com.bretth.osmosis.core.customdb.v0_5.impl.MultipleSourceIterator;
import com.bretth.osmosis.core.customdb.v0_5.impl.UpcastIterator;
import com.bretth.osmosis.core.domain.v0_5.Node;
import com.bretth.osmosis.core.domain.v0_5.Relation;
import com.bretth.osmosis.core.domain.v0_5.Way;
import com.bretth.osmosis.core.filter.v0_5.impl.BaseDatasetReader;
import com.bretth.osmosis.core.store.ReleasableIterator;


/**
 * Exposes a Berkeley Database as a dataset reader.
 * 
 * @author Brett Henderson
 */
public class BdbDatasetReader extends BaseDatasetReader {
	
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
	protected ReleasableIterator<Long> getNodeIdsForTileRange(int minimumTile, int maximumTile) {
		return txnCtx.getNodeDao().getNodeIdsForTile(minimumTile, maximumTile);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected ReleasableIterator<Long> getWayIdsForTileRange(int minimumTile, int maximumTile) {
		throw new UnsupportedOperationException();
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected ReleasableIterator<Long> getWayIdsOwningNode(long nodeId) {
		return txnCtx.getWayDao().getWayIdsOwningNode(nodeId);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected ReleasableIterator<Long> getRelationIdsOwningNode(long nodeId) {
		return txnCtx.getRelationDao().getRelationIdsOwningNode(nodeId);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected ReleasableIterator<Long> getRelationIdsOwningRelation(long relationId) {
		return txnCtx.getRelationDao().getRelationIdsOwningRelation(relationId);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected ReleasableIterator<Long> getRelationIdsOwningWay(long wayId) {
		return txnCtx.getRelationDao().getRelationIdsOwningWay(wayId);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected boolean isTileWayIndexAvailable() {
		return false;
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
	public ReleasableIterator<EntityContainer> iterate() {
		List<ReleasableIterator<EntityContainer>> sources;
		
		sources = new ArrayList<ReleasableIterator<EntityContainer>>();
		
		sources.add(new UpcastIterator<EntityContainer, NodeContainer>(new NodeContainerIterator(txnCtx.getNodeDao().iterate())));
		sources.add(new UpcastIterator<EntityContainer, WayContainer>(new WayContainerIterator(txnCtx.getWayDao().iterate())));
		sources.add(new UpcastIterator<EntityContainer, RelationContainer>(new RelationContainerIterator(txnCtx.getRelationDao().iterate())));
		
		return new MultipleSourceIterator<EntityContainer>(sources);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void release() {
		txnCtx.release();
	}
}
