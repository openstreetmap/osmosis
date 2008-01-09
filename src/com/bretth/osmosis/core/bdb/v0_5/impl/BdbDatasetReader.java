// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package com.bretth.osmosis.core.bdb.v0_5.impl;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import com.bretth.osmosis.core.container.v0_5.DatasetReader;
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
import com.bretth.osmosis.core.domain.v0_5.WayNode;
import com.bretth.osmosis.core.filter.common.BitSetIdTracker;
import com.bretth.osmosis.core.filter.common.IdTracker;
import com.bretth.osmosis.core.mysql.common.TileCalculator;
import com.bretth.osmosis.core.store.EmptyIterator;
import com.bretth.osmosis.core.store.ReleasableIterator;
import com.bretth.osmosis.core.store.UnsignedIntegerComparator;


/**
 * Exposes a Berkeley Database as a dataset reader.
 * 
 * @author Brett Henderson
 */
public class BdbDatasetReader implements DatasetReader {
	
	private TransactionContext txnCtx;
	private TileCalculator tileCalculator;
	private Comparator<Integer> tileOrdering;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param transactionContext
	 *            Provides access to the underlying data store.
	 */
	public BdbDatasetReader(TransactionContext transactionContext) {
		this.txnCtx = transactionContext;
		
		tileCalculator = new TileCalculator();
		tileOrdering = new UnsignedIntegerComparator();
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
	 * Determines if a node lies within the bounding box.
	 * 
	 * @param boundingBox
	 *            The bounding box.
	 * @param node
	 *            The node to be checked.
	 * @return True if the node lies within the box.
	 */
	private boolean isNodeInsideBox(Rectangle2D boundingBox, Node node) {
		return boundingBox.contains(node.getLongitude(), node.getLatitude());
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public ReleasableIterator<EntityContainer> iterateBoundingBox(double left, double right, double top, double bottom, boolean completeWays) {
		Rectangle2D boundingBox;
		int calculatedTile;
		int maximumTile;
		int minimumTile;
		IdTracker nodeIdTracker;
		IdTracker externalNodeIdTracker;
		IdTracker wayIdTracker;
		IdTracker relationIdTracker;
		ReleasableIterator<Long> nodeTileIndexValues;
		
		// Verify that the input coordinates create a positive box, if not just
		// return an empty result set.
		if (left > right || bottom > top) {
			return new EmptyIterator<EntityContainer>();
		}
		
		// Create a rectangle representing the bounding box.
		boundingBox = new Rectangle2D.Double(left, bottom, right - left, top - bottom);
		
		// Calculate the maximum and minimum tile values for the bounding box.
		calculatedTile = (int) tileCalculator.calculateTile(top, left);
		maximumTile = calculatedTile;
		minimumTile = calculatedTile;
		
		calculatedTile = (int) tileCalculator.calculateTile(top, right);
		if (tileOrdering.compare(calculatedTile, minimumTile) < 0) {
			minimumTile = calculatedTile;
		}
		if (tileOrdering.compare(calculatedTile, maximumTile) > 0) {
			maximumTile = calculatedTile;
		}
		
		calculatedTile = (int) tileCalculator.calculateTile(bottom, left);
		if (tileOrdering.compare(calculatedTile, minimumTile) < 0) {
			minimumTile = calculatedTile;
		}
		if (tileOrdering.compare(calculatedTile, maximumTile) > 0) {
			maximumTile = calculatedTile;
		}
		
		calculatedTile = (int) tileCalculator.calculateTile(bottom, right);
		if (tileOrdering.compare(calculatedTile, minimumTile) < 0) {
			minimumTile = calculatedTile;
		}
		if (tileOrdering.compare(calculatedTile, maximumTile) > 0) {
			maximumTile = calculatedTile;
		}
		
		// The tile values at the corners are all zero. If max tile is 0 but if
		// the maximum longitude and latitude are above minimum values set the
		// maximum tile to the maximum value.
		if (maximumTile == 0) {
			if (right > -180 || top > -90) {
				maximumTile = 0xFFFFFFFF;
			}
		}
		
		// Search through all nodes in the tile range and store the ids of those
		// within the bounding box.
		nodeIdTracker = new BitSetIdTracker();
		nodeTileIndexValues = txnCtx.getNodeDao().getNodeIdsForTile(minimumTile, maximumTile);
		try {
			while (nodeTileIndexValues.hasNext()) {
				long nodeId;
				Node node;
				
				// Get the next node id.
				nodeId = nodeTileIndexValues.next();
				
				// If the node id is new, check if it's inside the bounding box
				// and add it to the id tracker if it is.
				if (!nodeIdTracker.get(nodeId)) {
					node = getNode(nodeId);
					
					// Determine if the node lies within the required bounding box.
					if (isNodeInsideBox(boundingBox, node)) {
						nodeIdTracker.set(nodeId);
					}
				}
			}
			
		} finally {
			nodeTileIndexValues.release();
		}
		
		// Select all ways that contain the currently selected nodes.
		wayIdTracker = new BitSetIdTracker();
		externalNodeIdTracker = new BitSetIdTracker();
		for (Long nodeId : nodeIdTracker) {
			ReleasableIterator<Long> wayIdIterator = txnCtx.getWayDao().getWayIdsOwningNode(nodeId);
			try {
				while (wayIdIterator.hasNext()) {
					long wayId;
					
					wayId = wayIdIterator.next();
					
					wayIdTracker.set(wayId);
					
					// If we want complete ways, we need to load each way and
					// check the list of nodes adding any nodes that haven't
					// already been selected (ie. those that are outside the box).
					if (completeWays) {
						Way way;
						
						way = getWay(wayId);
						
						for (WayNode wayNode : way.getWayNodeList()) {
							long externalNodeId;
							
							externalNodeId = wayNode.getNodeId();
							
							if (!nodeIdTracker.get(externalNodeId)) {
								externalNodeIdTracker.set(externalNodeId);
							}
						}
					}
				}
				
			} finally {
				wayIdIterator.release();
			}
		}
		
		// Select all relations that contain the currently selected nodes, ways and relations.
		relationIdTracker = new BitSetIdTracker();
		for (Long nodeId : nodeIdTracker) {
			ReleasableIterator<Long> relationIdIterator = txnCtx.getRelationDao().getRelationIdsOwningNode(nodeId);
			try {
				while (relationIdIterator.hasNext()) {
					relationIdTracker.set(relationIdIterator.next());
				}
				
			} finally {
				relationIdIterator.release();
			}
		}
		for (Long wayId : wayIdTracker) {
			ReleasableIterator<Long> relationIdIterator = txnCtx.getRelationDao().getRelationIdsOwningWay(wayId);
			try {
				while (relationIdIterator.hasNext()) {
					relationIdTracker.set(relationIdIterator.next());
				}
				
			} finally {
				relationIdIterator.release();
			}
		}
		for (boolean moreParents = true; moreParents; ) {
			// If parents of current relations are found, this flag will be set
			// triggering another round of searching.
			moreParents = false;
			
			for (Long relationId : relationIdTracker) {
				ReleasableIterator<Long> relationIdIterator = txnCtx.getRelationDao().getRelationIdsOwningRelation(relationId);
				try {
					while (relationIdIterator.hasNext()) {
						long parentRelationId;
						
						parentRelationId = relationIdIterator.next();
						
						if (!relationIdTracker.get(parentRelationId)) {
							relationIdTracker.set(parentRelationId);
							moreParents = true;
						}
					}
					
				} finally {
					relationIdIterator.release();
				}
			}
		}
		
		// Now we need to add any external nodes that might have been included outside the bounding box.
		nodeIdTracker.setAll(externalNodeIdTracker);
		
		return new ResultIterator(nodeIdTracker, wayIdTracker, relationIdTracker);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void release() {
		txnCtx.release();
	}
	
	
	/**
	 * Returns a complete result set of matching records based on lists of
	 * entity ids. It will return the nodes, followed by the ways, followed by
	 * the relations.
	 * 
	 * @author Brett Henderson
	 */
	private class ResultIterator implements ReleasableIterator<EntityContainer> {
		private Iterator<Long> nodeIds;
		private Iterator<Long> wayIds;
		private Iterator<Long> relationIds;
		
		
		/**
		 * Creates a new instance.
		 * 
		 * @param nodeIdList
		 *            The set of nodes to be returned.
		 * @param wayIdList
		 *            The set of ways to be returned.
		 * @param relationIdList
		 *            The set of relations to be returned.
		 */
		public ResultIterator(IdTracker nodeIdList, IdTracker wayIdList, IdTracker relationIdList) {
			nodeIds = nodeIdList.iterator();
			wayIds = wayIdList.iterator();
			relationIds = relationIdList.iterator();
		}
		
		
		/**
		 * {@inheritDoc}
		 */
		@Override
		public boolean hasNext() {
			return (nodeIds.hasNext() || wayIds.hasNext() || relationIds.hasNext());
		}
		
		
		/**
		 * {@inheritDoc}
		 */
		@Override
		public EntityContainer next() {
			if (nodeIds.hasNext()) {
				return new NodeContainer(getNode(nodeIds.next()));
			}
			if (wayIds.hasNext()) {
				return new WayContainer(getWay(wayIds.next()));
			}
			if (relationIds.hasNext()) {
				return new RelationContainer(getRelation(relationIds.next()));
			}
			
			throw new NoSuchElementException();
		}
		
		
		/**
		 * {@inheritDoc}
		 */
		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
		
		
		/**
		 * {@inheritDoc}
		 */
		@Override
		public void release() {
			// Do nothing.
		}
	}
}
