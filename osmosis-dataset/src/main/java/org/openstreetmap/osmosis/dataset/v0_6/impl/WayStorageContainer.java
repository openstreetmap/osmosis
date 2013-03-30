// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.dataset.v0_6.impl;

import org.openstreetmap.osmosis.core.domain.v0_6.Way;
import org.openstreetmap.osmosis.core.lifecycle.Releasable;
import org.openstreetmap.osmosis.core.lifecycle.ReleasableContainer;
import org.openstreetmap.osmosis.core.store.IndexStoreReader;
import org.openstreetmap.osmosis.core.store.LongLongIndexElement;
import org.openstreetmap.osmosis.core.store.RandomAccessObjectStoreReader;


/**
 * Holds references to all of the node storage related classes.
 * 
 * @author Brett Henderson
 */
public class WayStorageContainer implements Releasable {
	private ReleasableContainer releasableContainer;
	private RandomAccessObjectStoreReader<Way> wayObjectReader;
	private IndexStoreReader<Long, LongLongIndexElement> wayObjectOffsetIndexReader;
	private WayTileAreaIndexReader wayTileIndexReader;
	private IndexStoreReader<Long, LongLongIndexElement> wayRelationIndexReader;


	/**
	 * Creates a new instance.
	 * 
	 * @param wayObjectReader
	 *            The raw way objects.
	 * @param wayObjectOffsetIndexReader
	 *            The way object offsets.
	 * @param wayTileIndexReader
	 *            The tile to way index.
	 * @param wayRelationIndexReader
	 *            The way to relation index.
	 */
	public WayStorageContainer(RandomAccessObjectStoreReader<Way> wayObjectReader,
			IndexStoreReader<Long, LongLongIndexElement> wayObjectOffsetIndexReader,
			WayTileAreaIndexReader wayTileIndexReader,
			IndexStoreReader<Long, LongLongIndexElement> wayRelationIndexReader) {
		
		releasableContainer = new ReleasableContainer();
		
		this.wayObjectReader = releasableContainer.add(wayObjectReader);
		this.wayObjectOffsetIndexReader = releasableContainer.add(wayObjectOffsetIndexReader);
		this.wayTileIndexReader = releasableContainer.add(wayTileIndexReader);
		this.wayRelationIndexReader = releasableContainer.add(wayRelationIndexReader);
	}


	/**
	 * Gets the raw way reader.
	 * 
	 * @return The raw way reader.
	 */
	public RandomAccessObjectStoreReader<Way> getWayObjectReader() {
		return wayObjectReader;
	}


	/**
	 * Gets the way object offset reader.
	 * 
	 * @return The way object offset reader.
	 */
	public IndexStoreReader<Long, LongLongIndexElement> getWayObjectOffsetIndexReader() {
		return wayObjectOffsetIndexReader;
	}


	/**
	 * Gets the tile to way index reader.
	 * 
	 * @return The tile to way index reader.
	 */
	public WayTileAreaIndexReader getWayTileIndexReader() {
		return wayTileIndexReader;
	}


	/**
	 * Gets the way to relation index reader.
	 * 
	 * @return The way to relation index reader.
	 */
	public IndexStoreReader<Long, LongLongIndexElement> getWayRelationIndexReader() {
		return wayRelationIndexReader;
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public void release() {
		releasableContainer.release();
	}
}
