// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.dataset.v0_6.impl;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.openstreetmap.osmosis.core.container.v0_6.DatasetContext;
import org.openstreetmap.osmosis.core.container.v0_6.EntityContainer;
import org.openstreetmap.osmosis.core.container.v0_6.EntityManager;
import org.openstreetmap.osmosis.core.container.v0_6.NodeContainer;
import org.openstreetmap.osmosis.core.container.v0_6.NodeContainerIterator;
import org.openstreetmap.osmosis.core.container.v0_6.RelationContainer;
import org.openstreetmap.osmosis.core.container.v0_6.RelationContainerIterator;
import org.openstreetmap.osmosis.core.container.v0_6.WayContainer;
import org.openstreetmap.osmosis.core.container.v0_6.WayContainerIterator;
import org.openstreetmap.osmosis.core.domain.v0_6.Node;
import org.openstreetmap.osmosis.core.domain.v0_6.Relation;
import org.openstreetmap.osmosis.core.domain.v0_6.Way;
import org.openstreetmap.osmosis.core.domain.v0_6.WayNode;
import org.openstreetmap.osmosis.core.filter.common.BitSetIdTracker;
import org.openstreetmap.osmosis.core.filter.common.IdTracker;
import org.openstreetmap.osmosis.core.lifecycle.ReleasableIterator;
import org.openstreetmap.osmosis.core.store.EmptyIterator;
import org.openstreetmap.osmosis.core.store.MultipleSourceIterator;
import org.openstreetmap.osmosis.core.store.NoSuchIndexElementException;
import org.openstreetmap.osmosis.core.store.ReleasableAdaptorForIterator;
import org.openstreetmap.osmosis.core.store.UpcastIterator;


/**
 * Provides read-only access to a dataset store. Each thread accessing the store
 * must create its own reader. The reader maintains all references to
 * heavyweight resources such as file handles used to access the store
 * eliminating the need for objects such as object iterators to be cleaned up
 * explicitly.
 * 
 * @author Brett Henderson
 */
public class DatasetStoreReader implements DatasetContext {
	
	private static final Logger LOG = Logger.getLogger(DatasetStoreReader.class.getName());
	
	private NodeStorageContainer nodeStorageContainer;
	private WayStorageContainer wayStorageContainer;
	private RelationStorageContainer relationStorageContainer;
	private NodeManager nodeManager;
	private WayManager wayManager;
	private RelationManager relationManager;
	
	private boolean enableWayTileIndex;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param nodeStorageContainer
	 *            The node storages.
	 * @param wayStorageContainer
	 *            The way storages.
	 * @param relationStorageContainer
	 *            The relation storages.
	 * @param enableWayTileIndex
	 *            If true a tile index is created for ways, otherwise a node-way
	 *            index is used.
	 */
	public DatasetStoreReader(
			NodeStorageContainer nodeStorageContainer,
			WayStorageContainer wayStorageContainer,
			RelationStorageContainer relationStorageContainer,
			boolean enableWayTileIndex) {
		this.nodeStorageContainer = nodeStorageContainer;
		this.wayStorageContainer = wayStorageContainer;
		this.relationStorageContainer = relationStorageContainer;
		
		this.enableWayTileIndex = enableWayTileIndex;
		
		nodeManager = new NodeManager(nodeStorageContainer);
		wayManager = new WayManager(wayStorageContainer);
		relationManager = new RelationManager(relationStorageContainer);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	private ReleasableIterator<Long> getNodeIdsForTileRange(int minimumTile, int maximumTile) {
		return new TileIndexValueIdIterator(
				nodeStorageContainer.getNodeTileIndexReader().getRange(minimumTile, maximumTile));
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	private ReleasableIterator<Long> getWayIdsForTileRange(int minimumTile, int maximumTile) {
		return new ReleasableAdaptorForIterator<Long>(
				wayStorageContainer.getWayTileIndexReader().getRange(minimumTile, maximumTile));
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	private ReleasableIterator<Long> getWayIdsOwningNode(long nodeId) {
		return new RelationalIndexValueIdIterator(
				nodeStorageContainer.getNodeWayIndexReader().getRange(nodeId, nodeId));
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	private ReleasableIterator<Long> getRelationIdsOwningNode(long nodeId) {
		return new RelationalIndexValueIdIterator(
				nodeStorageContainer.getNodeRelationIndexReader().getRange(nodeId, nodeId));
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	private ReleasableIterator<Long> getRelationIdsOwningWay(long wayId) {
		return new RelationalIndexValueIdIterator(
				wayStorageContainer.getWayRelationIndexReader().getRange(wayId, wayId));
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	private ReleasableIterator<Long> getRelationIdsOwningRelation(long relationId) {
		return new RelationalIndexValueIdIterator(
				relationStorageContainer.getRelationRelationIndexReader().getRange(relationId, relationId));
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	private boolean isTileWayIndexAvailable() {
		return enableWayTileIndex;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	@Deprecated
	public Node getNode(long id) {
		return nodeManager.getEntity(id);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	@Deprecated
	public Way getWay(long id) {
		return wayManager.getEntity(id);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	@Deprecated
	public Relation getRelation(long id) {
		return relationManager.getEntity(id);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public EntityManager<Node> getNodeManager() {
		return nodeManager;
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public EntityManager<Way> getWayManager() {
		return wayManager;
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public EntityManager<Relation> getRelationManager() {
		return relationManager;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public ReleasableIterator<EntityContainer> iterate() {
		List<ReleasableIterator<EntityContainer>> sources;
		
		sources = new ArrayList<ReleasableIterator<EntityContainer>>();
		
		sources.add(
				new UpcastIterator<EntityContainer, NodeContainer>(
						new NodeContainerIterator(nodeManager.iterate())));
		sources.add(
				new UpcastIterator<EntityContainer, WayContainer>(
						new WayContainerIterator(wayManager.iterate())));
		sources.add(
				new UpcastIterator<EntityContainer, RelationContainer>(
						new RelationContainerIterator(relationManager.iterate())));
		
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
	 * Determines if a way lies within the bounding box.
	 * 
	 * @param boundingBox
	 *            The bounding box.
	 * @param nodes
	 *            The ordered nodes of the way in order.
	 * @return True if the way is at least partially within the box.
	 */
	private boolean isWayInsideBox(Rectangle2D boundingBox, List<Node> nodes) {
		// If at least one node lies within the box, the way is inside the box.
		for (Node node : nodes) {
			if (isNodeInsideBox(boundingBox, node)) {
				return true;
			}
		}
		
		// Now we need to check if any of the segments cross the box.
		for (int i = 0; i < nodes.size() - 1; i++) {
			Node nodeA;
			Node nodeB;
			
			nodeA = nodes.get(i);
			nodeB = nodes.get(i + 1);
			
			if (boundingBox.intersectsLine(nodeA.getLongitude(), nodeA.getLatitude(), nodeB.getLongitude(),
					nodeB.getLatitude())) {
				return true;
			}
		}
		
		return false;
	}
	
	
	/**
	 * Retrieves all nodes for the bounding box and populates the node id
	 * tracker.
	 * 
	 * @param bboxCtx
	 *            The bounding box data.
	 */
	private void populateNodeIds(BoundingBoxContext bboxCtx) {
		ReleasableIterator<Long> nodeIdsForTileset;
		IdTracker idTracker;
		
		idTracker = new BitSetIdTracker();
		
		// Search through all nodes in the tile range and add them to a
		// temporary id tracker. This temporary id tracker allows all node ids
		// to be sorted ascendingly prior to retrieving the nodes themselves
		// which improves index performance.
		nodeIdsForTileset = getNodeIdsForTileRange(bboxCtx.minimumTile, bboxCtx.maximumTile);
		try {
			while (nodeIdsForTileset.hasNext()) {
				idTracker.set(nodeIdsForTileset.next());
			}
			
		} finally {
			nodeIdsForTileset.release();
		}
		
		// Check to see whether each applicable node lies within the bounding
		// box and add them to the result id list if they are.
		for (long nodeId : idTracker) {
			Node node = getNode(nodeId);
			
			// Determine if the node lies within the required bounding box.
			if (isNodeInsideBox(bboxCtx.boundingBox, node)) {
				bboxCtx.nodeIdTracker.set(nodeId);
			}
		}
	}
	
	
	/**
	 * Retrieves all ways for the bounding box, populates the way id tracker,
	 * and updates the external node tracker with any nodes outside the box if
	 * complete ways are required.
	 * 
	 * @param bboxCtx
	 *            The bounding box data.
	 */
	private void populateWayIdsUsingTileWayIndex(BoundingBoxContext bboxCtx, boolean completeWays) {
		ReleasableIterator<Long> tileWayIndexValues;
		
		// Search through all ways in the tile range and store the ids of those
		// within the bounding box.
		tileWayIndexValues = getWayIdsForTileRange(bboxCtx.minimumTile, bboxCtx.maximumTile);
		try {
			while (tileWayIndexValues.hasNext()) {
				long wayId;
				Way way;
				List<Node> nodes;
				
				// Load the current way.
				wayId = tileWayIndexValues.next();
				way = getWay(wayId);
				
				// Load the nodes within the way.
				nodes = new ArrayList<Node>();
				for (WayNode wayNode : way.getWayNodes()) {
					try {
						nodes.add(getNode(wayNode.getNodeId()));
					} catch (NoSuchIndexElementException e) {
						// Ignore any referential integrity problems.
						if (LOG.isLoggable(Level.FINER)) {
							LOG.finest(
								"Ignoring referential integrity problem where way " + wayId
								+ " refers to non-existent node " + wayNode.getNodeId() + "."
							);
						}
					}
				}
				
				// Determine if the way lies within the required bounding box.
				if (isWayInsideBox(bboxCtx.boundingBox, nodes)) {
					bboxCtx.wayIdTracker.set(wayId);
					
					// If we want complete ways, we need to check the list of nodes
					// adding any nodes that haven't already been selected (ie.
					// those that are outside the box).
					if (completeWays) {
						for (WayNode wayNode : way.getWayNodes()) {
							long nodeId;
							
							nodeId = wayNode.getNodeId();
							
							if (!bboxCtx.nodeIdTracker.get(nodeId)) {
								bboxCtx.externalNodeIdTracker.set(nodeId);
							}
						}
					}
				}
			}
		} finally {
			tileWayIndexValues.release();
		}
	}
	
	
	/**
	 * Retrieves all ways for the currently selected nodes, populates the way id
	 * tracker, and updates the external node tracker with any nodes outside the
	 * box if complete ways are required.
	 * 
	 * @param bboxCtx
	 *            The bounding box data.
	 */
	private void populateWayIdsUsingNodeWayIndex(BoundingBoxContext bboxCtx, boolean completeWays) {
		// Select all ways that contain the currently selected nodes.
		for (Long nodeId : bboxCtx.nodeIdTracker) {
			ReleasableIterator<Long> wayIdIterator = getWayIdsOwningNode(nodeId);
			try {
				while (wayIdIterator.hasNext()) {
					bboxCtx.wayIdTracker.set(wayIdIterator.next());
				}
				
			} finally {
				wayIdIterator.release();
			}
		}
		
		// If we want complete ways, we need to load each way and
		// check the list of nodes adding any nodes that haven't
		// already been selected (ie. those that are outside the box).
		// This is done outside the main loop so that ways are loaded
		// in ascending order which utilises index caching more effectively
		if (completeWays) {
			for (Long wayId : bboxCtx.wayIdTracker) {
				Way way;
				
				way = getWay(wayId);
				
				for (WayNode wayNode : way.getWayNodes()) {
					long externalNodeId;
					
					externalNodeId = wayNode.getNodeId();
					
					if (!bboxCtx.nodeIdTracker.get(externalNodeId)) {
						bboxCtx.externalNodeIdTracker.set(externalNodeId);
					}
				}
			}
		}
	}
	
	
	/**
	 * Retrieves all relations for the currently selected nodes and ways,
	 * populates the relation id tracker, and recursively includes all parents
	 * of selected relations.
	 * 
	 * @param bboxCtx
	 *            The bounding box data.
	 */
	private void populateRelationIds(BoundingBoxContext bboxCtx) {
		// Select all relations that contain the currently selected nodes, ways and relations.
		for (Long nodeId : bboxCtx.nodeIdTracker) {
			ReleasableIterator<Long> relationIdIterator = getRelationIdsOwningNode(nodeId);
			try {
				while (relationIdIterator.hasNext()) {
					bboxCtx.relationIdTracker.set(relationIdIterator.next());
				}
				
			} finally {
				relationIdIterator.release();
			}
		}
		for (Long wayId : bboxCtx.wayIdTracker) {
			ReleasableIterator<Long> relationIdIterator = getRelationIdsOwningWay(wayId);
			try {
				while (relationIdIterator.hasNext()) {
					bboxCtx.relationIdTracker.set(relationIdIterator.next());
				}
				
			} finally {
				relationIdIterator.release();
			}
		}
		for (boolean moreParents = true; moreParents;) {
			// If parents of current relations are found, this flag will be set
			// triggering another round of searching.
			moreParents = false;
			
			for (Long relationId : bboxCtx.relationIdTracker) {
				ReleasableIterator<Long> relationIdIterator = getRelationIdsOwningRelation(relationId);
				try {
					while (relationIdIterator.hasNext()) {
						long parentRelationId;
						
						parentRelationId = relationIdIterator.next();
						
						if (!bboxCtx.relationIdTracker.get(parentRelationId)) {
							bboxCtx.relationIdTracker.set(parentRelationId);
							moreParents = true;
						}
					}
					
				} finally {
					relationIdIterator.release();
				}
			}
		}
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public ReleasableIterator<EntityContainer> iterateBoundingBox(
			double left, double right, double top, double bottom, boolean completeWays) {
		BoundingBoxContext bboxCtx;
		
		LOG.fine("Beginning bounding box iteration.");
		
		// Create the bounding box context to manage the data associated with
		// this call.
		bboxCtx = new BoundingBoxContext(left, right, top, bottom);
		
		// Verify that the input coordinates create a positive box, if not just
		// return an empty result set.
		if (left > right || bottom > top) {
			LOG.fine("Bounding box is zero size, returning an empty iterator.");
			return new EmptyIterator<EntityContainer>();
		}
		
		LOG.fine("Populating node ids.");
		populateNodeIds(bboxCtx);
		
		if (isTileWayIndexAvailable()) {
			LOG.fine("Populating way ids using tile-way index.");
			populateWayIdsUsingTileWayIndex(bboxCtx, completeWays);
		} else {
			LOG.fine("Populating way ids using node-way index.");
			populateWayIdsUsingNodeWayIndex(bboxCtx, completeWays);
		}
		
		LOG.fine("Populating relation ids.");
		populateRelationIds(bboxCtx);
		
		// Now we need to add any external nodes that might have been included outside the bounding box.
		bboxCtx.nodeIdTracker.setAll(bboxCtx.externalNodeIdTracker);
		
		LOG.fine("Iterating all entities matching result ids.");
		return new ResultIterator(bboxCtx.nodeIdTracker, bboxCtx.wayIdTracker, bboxCtx.relationIdTracker);
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


	/**
	 * {@inheritDoc}
	 */
	@Override
	public void complete() {
		// This dataset is read-only so no changes need to be committed.
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void release() {
		nodeStorageContainer.release();
		wayStorageContainer.release();
		relationStorageContainer.release();
	}
}
