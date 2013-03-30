// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.dataset.v0_6.impl;

import org.openstreetmap.osmosis.core.domain.v0_6.Node;
import org.openstreetmap.osmosis.core.lifecycle.Releasable;
import org.openstreetmap.osmosis.core.lifecycle.ReleasableContainer;
import org.openstreetmap.osmosis.core.store.IndexStoreReader;
import org.openstreetmap.osmosis.core.store.IntegerLongIndexElement;
import org.openstreetmap.osmosis.core.store.LongLongIndexElement;
import org.openstreetmap.osmosis.core.store.RandomAccessObjectStoreReader;

/**
 * Holds references to all of the node storage related classes.
 * 
 * @author Brett Henderson
 */
public class NodeStorageContainer implements Releasable {
	private ReleasableContainer releasableContainer;
	private RandomAccessObjectStoreReader<Node> nodeObjectReader;
	private IndexStoreReader<Long, LongLongIndexElement> nodeObjectOffsetIndexReader;
	private IndexStoreReader<Integer, IntegerLongIndexElement> nodeTileIndexReader;
	private IndexStoreReader<Long, LongLongIndexElement> nodeWayIndexReader;
	private IndexStoreReader<Long, LongLongIndexElement> nodeRelationIndexReader;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param nodeObjectReader
	 *            The raw node objects.
	 * @param nodeObjectOffsetIndexReader
	 *            The node object offsets.
	 * @param nodeTileIndexReader
	 *            The tile to node index.
	 * @param nodeWayIndexReader
	 *            The node to way index.
	 * @param nodeRelationIndexReader
	 *            The node to relation index.
	 */
	public NodeStorageContainer(
			RandomAccessObjectStoreReader<Node> nodeObjectReader,
			IndexStoreReader<Long, LongLongIndexElement> nodeObjectOffsetIndexReader,
			IndexStoreReader<Integer, IntegerLongIndexElement> nodeTileIndexReader,
			IndexStoreReader<Long, LongLongIndexElement> nodeWayIndexReader,
			IndexStoreReader<Long, LongLongIndexElement> nodeRelationIndexReader) {
		
		releasableContainer = new ReleasableContainer();
		
		this.nodeObjectReader = releasableContainer.add(nodeObjectReader);
		this.nodeObjectOffsetIndexReader = releasableContainer.add(nodeObjectOffsetIndexReader);
		this.nodeTileIndexReader = releasableContainer.add(nodeTileIndexReader);
		this.nodeWayIndexReader = releasableContainer.add(nodeWayIndexReader);
		this.nodeRelationIndexReader = releasableContainer.add(nodeRelationIndexReader);
	}


	/**
	 * Gets the raw node reader.
	 * 
	 * @return The raw node reader.
	 */
	public RandomAccessObjectStoreReader<Node> getNodeObjectReader() {
		return nodeObjectReader;
	}


	/**
	 * Gets the node object offset reader.
	 * 
	 * @return The node object offset reader.
	 */
	public IndexStoreReader<Long, LongLongIndexElement> getNodeObjectOffsetIndexReader() {
		return nodeObjectOffsetIndexReader;
	}


	/**
	 * Gets the tile to node index reader.
	 * 
	 * @return The tile to node index reader.
	 */
	public IndexStoreReader<Integer, IntegerLongIndexElement> getNodeTileIndexReader() {
		return nodeTileIndexReader;
	}


	/**
	 * Gets the node to way index reader.
	 * 
	 * @return The node to way index reader.
	 */
	public IndexStoreReader<Long, LongLongIndexElement> getNodeWayIndexReader() {
		return nodeWayIndexReader;
	}


	/**
	 * Gets the node to relation index reader.
	 * 
	 * @return The node to relation index reader.
	 */
	public IndexStoreReader<Long, LongLongIndexElement> getNodeRelationIndexReader() {
		return nodeRelationIndexReader;
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public void release() {
		releasableContainer.release();
	}
}
