// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.sort.common;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;

import org.openstreetmap.osmosis.core.lifecycle.ReleasableIterator;


/**
 * This iterator examines a list of sorted input sources and merges them into a
 * single sorted list.
 * 
 * @param <DataType>
 *            The object type to be sorted.
 * @author Brett Henderson
 */
public class MergingIterator<DataType> implements ReleasableIterator<DataType> {
	private List<ReleasableIterator<DataType>> sources;
	private Comparator<DataType> comparator;
	private List<DataType> sourceData;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param sources
	 *            The list of data sources.
	 * @param comparator
	 *            The comparator to be used for sorting.
	 */
	public MergingIterator(List<ReleasableIterator<DataType>> sources, Comparator<DataType> comparator) {
		this.sources = new ArrayList<ReleasableIterator<DataType>>(sources);
		this.comparator = comparator;
	}
	
	
	/**
	 * Primes the sorting collections.
	 */
	private void initialize() {
		if (sourceData == null) {
			// Get the first entity from each source.  Delete any empty sources.
			sourceData = new ArrayList<DataType>(sources.size());
			for (int sourceIndex = 0; sourceIndex < sources.size();) {
				ReleasableIterator<DataType> source;
				
				source = sources.get(sourceIndex);
				
				if (source.hasNext()) {
					sourceData.add(source.next());
					sourceIndex++;
				} else {
					sources.remove(sourceIndex).release();
				}
			}
		}
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public boolean hasNext() {
		initialize();
		
		return sourceData.size() > 0;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public DataType next() {
		DataType dataMinimum;
		int indexMinimum;
		ReleasableIterator<DataType> source;
		
		initialize();
		
		if (!hasNext()) {
			throw new NoSuchElementException();
		}
		
		dataMinimum = sourceData.get(0);
		indexMinimum = 0;
		
		// Find the minimum entity.
		for (int indexCurrent = 1; indexCurrent < sources.size(); indexCurrent++) {
			DataType dataCurrent = sourceData.get(indexCurrent);
			
			// Check if the current data entity is less than the existing minimum.
			if (comparator.compare(dataMinimum, dataCurrent) > 0) {
				dataMinimum = dataCurrent;
				indexMinimum = indexCurrent;
			}
		}
		
		// Get the next entity from the source if available.
		// Otherwise remove the source and its current data.
		source = sources.get(indexMinimum);
		if (source.hasNext()) {
			sourceData.set(indexMinimum, source.next());
		} else {
			sources.remove(indexMinimum).release();
			sourceData.remove(indexMinimum);
		}
		
		return dataMinimum;
	}
	
	
	/**
	 * Not supported. An UnsupportedOperationException is always thrown.
	 */
	public void remove() {
		throw new UnsupportedOperationException();
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void release() {
		for (ReleasableIterator<DataType> source : sources) {
			source.release();
		}
	}
}
