// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package com.bretth.osmosis.core.customdb.v0_5.impl;

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
import com.bretth.osmosis.core.domain.v0_5.Node;
import com.bretth.osmosis.core.domain.v0_5.Relation;
import com.bretth.osmosis.core.domain.v0_5.Way;
import com.bretth.osmosis.core.domain.v0_5.WayNode;
import com.bretth.osmosis.core.filter.common.IdTracker;
import com.bretth.osmosis.core.filter.common.ListIdTracker;
import com.bretth.osmosis.core.mysql.common.TileCalculator;
import com.bretth.osmosis.core.store.EmptyIterator;
import com.bretth.osmosis.core.store.IndexStoreReader;
import com.bretth.osmosis.core.store.IntegerLongIndexElement;
import com.bretth.osmosis.core.store.LongLongIndexElement;
import com.bretth.osmosis.core.store.RandomAccessObjectStoreReader;


/**
 * Provides read-only access to a dataset store. Each thread accessing the store
 * must create its own reader. The reader maintains all references to
 * heavyweight resources such as file handles used to access the store
 * eliminating the need for objects such as object iterators to be cleaned up
 * explicitly.
 * 
 * @author Brett Henderson
 */
public class DatasetStoreReader implements DatasetReader {
	
	private RandomAccessObjectStoreReader<Node> nodeObjectReader;
	private IndexStoreReader<Long, LongLongIndexElement> nodeObjectOffsetIndexReader;
	private RandomAccessObjectStoreReader<Way> wayObjectReader;
	private IndexStoreReader<Long, LongLongIndexElement> wayObjectOffsetIndexReader;
	private RandomAccessObjectStoreReader<Relation> relationObjectReader;
	private IndexStoreReader<Long, LongLongIndexElement> relationObjectOffsetIndexReader;
	
	private TileCalculator tileCalculator;
	private Comparator<Integer> tileOrdering;
	private IndexStoreReader<Integer, IntegerLongIndexElement> nodeTileIndexReader;
	private WayTileAreaIndexReader wayTileIndexReader;
	private IndexStoreReader<Long, LongLongIndexElement> nodeRelationIndexReader;
	private IndexStoreReader<Long, LongLongIndexElement> wayRelationIndexReader;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param nodeObjectReader
	 *            The raw node objects.
	 * @param nodeObjectOffsetIndexReader
	 *            The node object offsets.
	 * @param wayObjectReader
	 *            The raw way objects.
	 * @param wayObjectOffsetIndexReader
	 *            The way object offsets.
	 * @param relationObjectReader
	 *            The raw relation objects.
	 * @param relationObjectOffsetIndexReader
	 *            The relation object offsets.
	 * @param tileCalculator
	 *            The tile index value calculator.
	 * @param tileOrdering
	 *            The ordering of tiles within the index.
	 * @param nodeTileIndexReader
	 *            The tile to node index.
	 * @param wayTileIndexReader
	 *            The tile to way index.
	 * @param nodeRelationIndexReader
	 *            The node to relation index.
	 * @param wayRelationIndexReader
	 *            The way to relation index.
	 */
	public DatasetStoreReader(RandomAccessObjectStoreReader<Node> nodeObjectReader, IndexStoreReader<Long, LongLongIndexElement> nodeObjectOffsetIndexReader, RandomAccessObjectStoreReader<Way> wayObjectReader, IndexStoreReader<Long, LongLongIndexElement> wayObjectOffsetIndexReader, RandomAccessObjectStoreReader<Relation> relationObjectReader, IndexStoreReader<Long, LongLongIndexElement> relationObjectOffsetIndexReader, TileCalculator tileCalculator, Comparator<Integer> tileOrdering, IndexStoreReader<Integer, IntegerLongIndexElement> nodeTileIndexReader, WayTileAreaIndexReader wayTileIndexReader, IndexStoreReader<Long, LongLongIndexElement> nodeRelationIndexReader, IndexStoreReader<Long, LongLongIndexElement> wayRelationIndexReader) {
		this.nodeObjectReader = nodeObjectReader;
		this.nodeObjectOffsetIndexReader = nodeObjectOffsetIndexReader;
		this.wayObjectReader = wayObjectReader;
		this.wayObjectOffsetIndexReader = wayObjectOffsetIndexReader;
		this.relationObjectReader = relationObjectReader;
		this.relationObjectOffsetIndexReader = relationObjectOffsetIndexReader;
		
		this.tileCalculator = tileCalculator;
		this.tileOrdering = tileOrdering;
		this.nodeTileIndexReader = nodeTileIndexReader;
		this.wayTileIndexReader = wayTileIndexReader;
		this.nodeRelationIndexReader = nodeRelationIndexReader;
		this.wayRelationIndexReader = wayRelationIndexReader;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Node getNode(long id) {
		return nodeObjectReader.get(
			nodeObjectOffsetIndexReader.get(id).getValue()
		);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Way getWay(long id) {
		return wayObjectReader.get(
			wayObjectOffsetIndexReader.get(id).getValue()
		);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Relation getRelation(long id) {
		return relationObjectReader.get(
			relationObjectOffsetIndexReader.get(id).getValue()
		);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Iterator<EntityContainer> iterate() {
		List<Iterator<EntityContainer>> sources;
		
		sources = new ArrayList<Iterator<EntityContainer>>();
		
		sources.add(new UpcastIterator<EntityContainer, NodeContainer>(new NodeContainerIterator(nodeObjectReader.iterate())));
		sources.add(new UpcastIterator<EntityContainer, WayContainer>(new WayContainerIterator(wayObjectReader.iterate())));
		sources.add(new UpcastIterator<EntityContainer, RelationContainer>(new RelationContainerIterator(relationObjectReader.iterate())));
		
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
			
			if (boundingBox.intersectsLine(nodeA.getLongitude(), nodeA.getLatitude(), nodeB.getLongitude(), nodeB.getLatitude())) {
				return true;
			}
		}
		
		return false;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Iterator<EntityContainer> iterateBoundingBox(double left, double right, double top, double bottom, boolean completeWays) {
		Rectangle2D boundingBox;
		int calculatedTile;
		int maximumTile;
		int minimumTile;
		IdTracker nodeIdTracker;
		IdTracker wayIdTracker;
		IdTracker relationIdTracker;
		Iterator<IntegerLongIndexElement> nodeTileIndexValues;
		Iterator<Long> wayTileIndexValues;
		
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
		
		// Search through all nodes in the tile range and store the ids of those
		// within the bounding box.
		nodeIdTracker = new ListIdTracker();
		nodeTileIndexValues = nodeTileIndexReader.getRange(minimumTile, maximumTile);
		while (nodeTileIndexValues.hasNext()) {
			long nodeId;
			Node node;
			
			// Load the current tile.
			nodeId = nodeTileIndexValues.next().getValue();
			node = getNode(nodeId);
			
			// Determine if the node lies within the required bounding box.
			if (isNodeInsideBox(boundingBox, node)) {
				nodeIdTracker.set(nodeId);
			}
		}
		
		// Search through all ways in the tile range and store the ids of those
		// within the bounding box.
		wayIdTracker = new ListIdTracker();
		wayTileIndexValues = wayTileIndexReader.getRange(minimumTile, maximumTile);
		while (nodeTileIndexValues.hasNext()) {
			long wayId;
			Way way;
			List<Node> nodes;
			
			// Load the current way.
			wayId = wayTileIndexValues.next();
			way = getWay(wayId);
			
			// Load the nodes within the way.
			nodes = new ArrayList<Node>();
			for (WayNode wayNode : way.getWayNodeList()) {
				nodes.add(getNode(wayNode.getNodeId()));
			}
			
			// Determine if the way lies within the required bounding box.
			if (isWayInsideBox(boundingBox, nodes)) {
				wayIdTracker.set(wayId);
				
				// If complete ways are required, ensure all nodes within the
				// way are marked for return.
				if (completeWays) {
					for (Node node : nodes) {
						nodeIdTracker.set(node.getId());
					}
				}
			}
		}
		
		// Select all relations that contain the currently selected nodes and ways.
		relationIdTracker = new ListIdTracker();
		for (Long nodeId : nodeIdTracker) {
			// Get all relation ids for relations containing the current node.
			for (Iterator<LongLongIndexElement> rangeIterator = nodeRelationIndexReader.getRange(nodeId, nodeId); rangeIterator.hasNext(); ) {
				relationIdTracker.set(rangeIterator.next().getValue());
			}
		}
		for (Long wayId : wayIdTracker) {
			// Get all relation ids for relations containing the current way.
			for (Iterator<LongLongIndexElement> rangeIterator = wayRelationIndexReader.getRange(wayId, wayId); rangeIterator.hasNext(); ) {
				relationIdTracker.set(rangeIterator.next().getValue());
			}
		}
		
		return new ResultIterator(nodeIdTracker, wayIdTracker, relationIdTracker);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void release() {
		nodeObjectReader.release();
		nodeObjectOffsetIndexReader.release();
	}
	
	
	/**
	 * Returns a complete result set of matching records based on lists of
	 * entity ids. It will return the nodes, followed by the ways, followed by
	 * the relations.
	 * 
	 * @author Brett Henderson
	 */
	private class ResultIterator implements Iterator<EntityContainer> {
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
	}
}
