// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package com.bretth.osmosis.core.customdb.v0_6.impl;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import com.bretth.osmosis.core.container.v0_6.EntityContainer;
import com.bretth.osmosis.core.container.v0_6.NodeContainer;
import com.bretth.osmosis.core.container.v0_6.NodeContainerIterator;
import com.bretth.osmosis.core.container.v0_6.RelationContainer;
import com.bretth.osmosis.core.container.v0_6.RelationContainerIterator;
import com.bretth.osmosis.core.container.v0_6.WayContainer;
import com.bretth.osmosis.core.container.v0_6.WayContainerIterator;
import com.bretth.osmosis.core.domain.v0_6.Node;
import com.bretth.osmosis.core.domain.v0_6.Relation;
import com.bretth.osmosis.core.domain.v0_6.Way;
import com.bretth.osmosis.core.filter.v0_6.impl.BaseDatasetReader;
import com.bretth.osmosis.core.lifecycle.ReleasableIterator;
import com.bretth.osmosis.core.mysql.common.TileCalculator;
import com.bretth.osmosis.core.store.IndexStoreReader;
import com.bretth.osmosis.core.store.IntegerLongIndexElement;
import com.bretth.osmosis.core.store.LongLongIndexElement;
import com.bretth.osmosis.core.store.MultipleSourceIterator;
import com.bretth.osmosis.core.store.RandomAccessObjectStoreReader;
import com.bretth.osmosis.core.store.ReleasableAdaptorForIterator;
import com.bretth.osmosis.core.store.UpcastIterator;


/**
 * Provides read-only access to a dataset store. Each thread accessing the store
 * must create its own reader. The reader maintains all references to
 * heavyweight resources such as file handles used to access the store
 * eliminating the need for objects such as object iterators to be cleaned up
 * explicitly.
 * 
 * @author Brett Henderson
 */
public class DatasetStoreReader extends BaseDatasetReader {
	
	private RandomAccessObjectStoreReader<Node> nodeObjectReader;
	private IndexStoreReader<Long, LongLongIndexElement> nodeObjectOffsetIndexReader;
	private RandomAccessObjectStoreReader<Way> wayObjectReader;
	private IndexStoreReader<Long, LongLongIndexElement> wayObjectOffsetIndexReader;
	private RandomAccessObjectStoreReader<Relation> relationObjectReader;
	private IndexStoreReader<Long, LongLongIndexElement> relationObjectOffsetIndexReader;
	
	private IndexStoreReader<Integer, IntegerLongIndexElement> nodeTileIndexReader;
	private WayTileAreaIndexReader wayTileIndexReader;
	private IndexStoreReader<Long, LongLongIndexElement> nodeWayIndexReader;
	private IndexStoreReader<Long, LongLongIndexElement> nodeRelationIndexReader;
	private IndexStoreReader<Long, LongLongIndexElement> wayRelationIndexReader;
	private IndexStoreReader<Long, LongLongIndexElement> relationRelationIndexReader;
	
	private boolean enableWayTileIndex;
	
	
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
	 * @param nodeWayIndexReader
	 *            The node to way index.
	 * @param nodeRelationIndexReader
	 *            The node to relation index.
	 * @param wayRelationIndexReader
	 *            The way to relation index.
	 * @param relationRelationIndexReader
	 *            The relation to relation index.
	 * @param enableWayTileIndex
	 *            If true a tile index is created for ways, otherwise a node-way
	 *            index is used.
	 */
	public DatasetStoreReader(
			RandomAccessObjectStoreReader<Node> nodeObjectReader,
			IndexStoreReader<Long, LongLongIndexElement> nodeObjectOffsetIndexReader,
			RandomAccessObjectStoreReader<Way> wayObjectReader,
			IndexStoreReader<Long, LongLongIndexElement> wayObjectOffsetIndexReader,
			RandomAccessObjectStoreReader<Relation> relationObjectReader,
			IndexStoreReader<Long, LongLongIndexElement> relationObjectOffsetIndexReader,
			TileCalculator tileCalculator, Comparator<Integer> tileOrdering,
			IndexStoreReader<Integer, IntegerLongIndexElement> nodeTileIndexReader,
			WayTileAreaIndexReader wayTileIndexReader, IndexStoreReader<Long, LongLongIndexElement> nodeWayIndexReader,
			IndexStoreReader<Long, LongLongIndexElement> nodeRelationIndexReader,
			IndexStoreReader<Long, LongLongIndexElement> wayRelationIndexReader,
			IndexStoreReader<Long, LongLongIndexElement> relationRelationIndexReader, boolean enableWayTileIndex) {
		this.nodeObjectReader = nodeObjectReader;
		this.nodeObjectOffsetIndexReader = nodeObjectOffsetIndexReader;
		this.wayObjectReader = wayObjectReader;
		this.wayObjectOffsetIndexReader = wayObjectOffsetIndexReader;
		this.relationObjectReader = relationObjectReader;
		this.relationObjectOffsetIndexReader = relationObjectOffsetIndexReader;
		
		this.nodeTileIndexReader = nodeTileIndexReader;
		this.wayTileIndexReader = wayTileIndexReader;
		this.nodeWayIndexReader = nodeWayIndexReader;
		this.nodeRelationIndexReader = nodeRelationIndexReader;
		this.wayRelationIndexReader = wayRelationIndexReader;
		this.relationRelationIndexReader = relationRelationIndexReader;
		
		this.enableWayTileIndex = enableWayTileIndex;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected ReleasableIterator<Long> getNodeIdsForTileRange(int minimumTile, int maximumTile) {
		return new TileIndexValueIdIterator(nodeTileIndexReader.getRange(minimumTile, maximumTile));
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected ReleasableIterator<Long> getWayIdsForTileRange(int minimumTile, int maximumTile) {
		return new ReleasableAdaptorForIterator<Long>(wayTileIndexReader.getRange(minimumTile, maximumTile));
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected ReleasableIterator<Long> getWayIdsOwningNode(long nodeId) {
		return new RelationalIndexValueIdIterator(nodeWayIndexReader.getRange(nodeId, nodeId));
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected ReleasableIterator<Long> getRelationIdsOwningNode(long nodeId) {
		return new RelationalIndexValueIdIterator(nodeRelationIndexReader.getRange(nodeId, nodeId));
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected ReleasableIterator<Long> getRelationIdsOwningWay(long wayId) {
		return new RelationalIndexValueIdIterator(wayRelationIndexReader.getRange(wayId, wayId));
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected ReleasableIterator<Long> getRelationIdsOwningRelation(long relationId) {
		return new RelationalIndexValueIdIterator(relationRelationIndexReader.getRange(relationId, relationId));
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected boolean isTileWayIndexAvailable() {
		return enableWayTileIndex;
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
	public ReleasableIterator<EntityContainer> iterate() {
		List<ReleasableIterator<EntityContainer>> sources;
		
		sources = new ArrayList<ReleasableIterator<EntityContainer>>();
		
		sources.add(
				new UpcastIterator<EntityContainer, NodeContainer>(
						new NodeContainerIterator(
								new ReleasableAdaptorForIterator<Node>(
										nodeObjectReader.iterate()))));
		sources.add(
				new UpcastIterator<EntityContainer, WayContainer>(
						new WayContainerIterator(
								new ReleasableAdaptorForIterator<Way>(
										wayObjectReader.iterate()))));
		sources.add(
				new UpcastIterator<EntityContainer, RelationContainer>(
						new RelationContainerIterator(
								new ReleasableAdaptorForIterator<Relation>(
										relationObjectReader.iterate()))));
		
		return new MultipleSourceIterator<EntityContainer>(sources);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void release() {
		nodeObjectReader.release();
		nodeObjectOffsetIndexReader.release();
	}
}
