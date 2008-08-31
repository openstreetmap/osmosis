// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package com.bretth.osmosis.core.customdb.v0_6.impl;

import java.util.ArrayList;
import java.util.List;

import com.bretth.osmosis.core.store.Completable;
import com.bretth.osmosis.core.store.IndexStore;
import com.bretth.osmosis.core.store.IndexStoreReader;
import com.bretth.osmosis.core.store.IntegerLongIndexElement;
import com.bretth.osmosis.core.store.Releasable;
import com.bretth.osmosis.core.store.ReleasableContainer;
import com.bretth.osmosis.core.store.UnsignedIntegerComparator;


/**
 * A class for managing a way tile index of varying granularity allowing
 * different sized tiles for different sized ways.
 */
public class WayTileAreaIndex implements Completable {
	private static final int[] masks = {0xFFFFFFFF, 0xFFFFFFF0, 0xFFFFFF00, 0xFFFF0000, 0xFF000000, 0x00000000};
	private List<IndexStore<Integer, IntegerLongIndexElement>> indexes;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param fileManager
	 *            Manages and provides files for writing indexes to.
	 */
	public WayTileAreaIndex(DatasetStoreFileManager fileManager) {
		indexes = new ArrayList<IndexStore<Integer, IntegerLongIndexElement>>(masks.length);
		
		for (int i = 0; i < masks.length; i++) {
			indexes.add(
				new IndexStore<Integer, IntegerLongIndexElement>(
					IntegerLongIndexElement.class,
					new UnsignedIntegerComparator(),
					fileManager.getWayTileIndexFile(i)
				)
			);
		}
	}
	
	
	/**
	 * Writes a new way tile entry to the index.
	 * 
	 * @param wayId
	 *            The way to be added.
	 * @param minimumTile
	 *            The minimum tile of the way.
	 * @param maximumTile
	 *            The maximum tile of the way.
	 */
	public void write(long wayId, int minimumTile, int maximumTile) {
		// Write a new index element for the tile to the tile index that matches
		// the granularity of the way.
		for (int i = 0; i < masks.length; i++) {
			int mask;
			int maskedMinimum;
			int maskedMaximum;
			
			mask = masks[i];
			maskedMinimum = mask & minimumTile;
			maskedMaximum = mask & maximumTile;
			
			// Write the element to the current index if the index tile
			// granularity allows the way to fit within a single tile value.
			if ((maskedMinimum) == (maskedMaximum)) {
				indexes.get(i).write(new IntegerLongIndexElement(maskedMinimum, wayId));
				// Stop once one index has received the way.
				break;
			}
		}
	}
	
	
	/**
	 * Creates a new reader capable of accessing the contents of this index. The
	 * reader must be explicitly released when no longer required. Readers must
	 * be released prior to this index.
	 * 
	 * @return An index reader.
	 */
	public WayTileAreaIndexReader createReader() {
		ReleasableContainer releasableContainer = new ReleasableContainer();
		
		try {
			List<IndexStoreReader<Integer, IntegerLongIndexElement>> indexReaders;
			
			indexReaders = new ArrayList<IndexStoreReader<Integer,IntegerLongIndexElement>>(masks.length);
			for (IndexStore<Integer, IntegerLongIndexElement> index : indexes) {
				indexReaders.add(releasableContainer.add(index.createReader()));
			}
			
			releasableContainer.clear();
			
			return new WayTileAreaIndexReader(masks, indexReaders);
			
		} finally {
			releasableContainer.release();
		}
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void complete() {
		for (Completable index : indexes) {
			index.complete();
		}
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void release() {
		for (Releasable index : indexes) {
			index.release();
		}
	}
}
