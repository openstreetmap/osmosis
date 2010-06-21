// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.dataset.v0_6.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import org.openstreetmap.osmosis.core.lifecycle.Releasable;
import org.openstreetmap.osmosis.core.store.IndexStoreReader;
import org.openstreetmap.osmosis.core.store.IntegerLongIndexElement;

/**
 * Provides read-only access to a way tile area index store. Each thread
 * accessing the store must create its own reader. The reader maintains all
 * references to heavyweight resources such as file handles used to access the
 * store eliminating the need for objects such as object iterators to be cleaned
 * up explicitly.
 * 
 * @author Brett Henderson
 */
public class WayTileAreaIndexReader implements Releasable {
	private int[] masks = {0xFFFFFFFF, 0xFFFFFFF0, 0xFFFFFF00, 0xFFFF0000, 0xFF000000, 0x00000000};
	private List<IndexStoreReader<Integer, IntegerLongIndexElement>> indexReaders;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param masks
	 *            The index masks.
	 * @param indexReaders
	 *            The index readers.
	 */
	public WayTileAreaIndexReader(int[] masks, List<IndexStoreReader<Integer, IntegerLongIndexElement>> indexReaders) {
		this.masks = masks;
		this.indexReaders = indexReaders;
	}
	
	
	/**
	 * Returns all elements in the range specified by the minimum and maximum
	 * tiles. All ways with tiles matching and lying between the two values will
	 * be returned.
	 * 
	 * @param minimumTile
	 *            The minimum tile in the range.
	 * @param maximumTile
	 *            The maximum tile in the range.
	 * @return An iterator pointing to the requested range.
	 */
	public Iterator<Long> getRange(Integer minimumTile, Integer maximumTile) {
		List<Iterator<IntegerLongIndexElement>> ranges;
		
		// Loop through the masks reading way id ranges from the corresponding
		// indexes.
		ranges = new ArrayList<Iterator<IntegerLongIndexElement>>(masks.length);
		for (int i = 0; i < masks.length; i++) {
			int mask;
			int beginKey;
			int endKey;
			IndexStoreReader<Integer, IntegerLongIndexElement> indexReader;
			
			mask = masks[i];
			beginKey = mask & minimumTile;
			endKey = mask & maximumTile;
			indexReader = indexReaders.get(i);
			
			ranges.add(indexReader.getRange(beginKey, endKey));
		}
		
		return new ResultIterator(ranges.iterator());
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void release() {
		for (IndexStoreReader<Integer, IntegerLongIndexElement> indexReader : indexReaders) {
			indexReader.release();
		}
	}
	
	
	/**
	 * Returns a complete result set of matching index values based on iterators
	 * from each of the internal indexes.
	 * 
	 * @author Brett Henderson
	 */
	private static class ResultIterator implements Iterator<Long> {
		private Iterator<Iterator<IntegerLongIndexElement>> sources;
		private Iterator<IntegerLongIndexElement> currentSource;
		private boolean currentSourceAvailable;
		
		
		/**
		 * Creates a new instance.
		 * 
		 * @param sources
		 *            The input sources.
		 */
		public ResultIterator(Iterator<Iterator<IntegerLongIndexElement>> sources) {
			this.sources = sources;
			
			currentSourceAvailable = false;
		}
		
		
		/**
		 * {@inheritDoc}
		 */
		@Override
		public boolean hasNext() {
			while (true) {
				// Get the next available input source if required.
				if (!currentSourceAvailable) {
					if (sources.hasNext()) {
						currentSource = sources.next();
						currentSourceAvailable = true;
					} else {
						return false;
					}
				}
				
				if (currentSource.hasNext()) {
					return true;
				} else {
					currentSourceAvailable = false;
				}
			}
		}
		
		
		/**
		 * {@inheritDoc}
		 */
		@Override
		public Long next() {
			if (!hasNext()) {
				throw new NoSuchElementException();
			}
			
			return currentSource.next().getValue();
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
