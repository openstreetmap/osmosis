// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.customdb.v0_5.impl;

import java.util.ArrayList;
import java.util.List;

import org.openstreetmap.osmosis.core.container.v0_5.EntityContainer;
import org.openstreetmap.osmosis.core.container.v0_5.NodeContainer;
import org.openstreetmap.osmosis.core.container.v0_5.NodeContainerIterator;
import org.openstreetmap.osmosis.core.container.v0_5.RelationContainer;
import org.openstreetmap.osmosis.core.container.v0_5.RelationContainerIterator;
import org.openstreetmap.osmosis.core.container.v0_5.WayContainer;
import org.openstreetmap.osmosis.core.container.v0_5.WayContainerIterator;
import org.openstreetmap.osmosis.core.domain.v0_5.Node;
import org.openstreetmap.osmosis.core.domain.v0_5.Relation;
import org.openstreetmap.osmosis.core.domain.v0_5.Way;
import org.openstreetmap.osmosis.core.filter.v0_5.impl.BaseDatasetReader;
import org.openstreetmap.osmosis.core.lifecycle.ReleasableIterator;
import org.openstreetmap.osmosis.core.store.MultipleSourceIterator;
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
public class DatasetStoreReader extends BaseDatasetReader {
	
	private NodeStorageContainer nodeStorageContainer;
	private WayStorageContainer wayStorageContainer;
	private RelationStorageContainer relationStorageContainer;
	
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
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected ReleasableIterator<Long> getNodeIdsForTileRange(int minimumTile, int maximumTile) {
		return new TileIndexValueIdIterator(
				nodeStorageContainer.getNodeTileIndexReader().getRange(minimumTile, maximumTile));
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected ReleasableIterator<Long> getWayIdsForTileRange(int minimumTile, int maximumTile) {
		return new ReleasableAdaptorForIterator<Long>(
				wayStorageContainer.getWayTileIndexReader().getRange(minimumTile, maximumTile));
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected ReleasableIterator<Long> getWayIdsOwningNode(long nodeId) {
		return new RelationalIndexValueIdIterator(
				nodeStorageContainer.getNodeWayIndexReader().getRange(nodeId, nodeId));
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected ReleasableIterator<Long> getRelationIdsOwningNode(long nodeId) {
		return new RelationalIndexValueIdIterator(
				nodeStorageContainer.getNodeRelationIndexReader().getRange(nodeId, nodeId));
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected ReleasableIterator<Long> getRelationIdsOwningWay(long wayId) {
		return new RelationalIndexValueIdIterator(
				wayStorageContainer.getWayRelationIndexReader().getRange(wayId, wayId));
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected ReleasableIterator<Long> getRelationIdsOwningRelation(long relationId) {
		return new RelationalIndexValueIdIterator(
				relationStorageContainer.getRelationRelationIndexReader().getRange(relationId, relationId));
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
		return nodeStorageContainer.getNodeObjectReader().get(
			nodeStorageContainer.getNodeObjectOffsetIndexReader().get(id).getValue()
		);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Way getWay(long id) {
		return wayStorageContainer.getWayObjectReader().get(
			wayStorageContainer.getWayObjectOffsetIndexReader().get(id).getValue()
		);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Relation getRelation(long id) {
		return relationStorageContainer.getRelationObjectReader().get(
			relationStorageContainer.getRelationObjectOffsetIndexReader().get(id).getValue()
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
										nodeStorageContainer.getNodeObjectReader().iterate()))));
		sources.add(
				new UpcastIterator<EntityContainer, WayContainer>(
						new WayContainerIterator(
								new ReleasableAdaptorForIterator<Way>(
										wayStorageContainer.getWayObjectReader().iterate()))));
		sources.add(
				new UpcastIterator<EntityContainer, RelationContainer>(
						new RelationContainerIterator(
								new ReleasableAdaptorForIterator<Relation>(
										relationStorageContainer.getRelationObjectReader().iterate()))));
		
		return new MultipleSourceIterator<EntityContainer>(sources);
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
