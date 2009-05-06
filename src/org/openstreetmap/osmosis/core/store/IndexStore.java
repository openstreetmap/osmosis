// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.store;

import java.io.File;
import java.util.Comparator;
import java.util.Iterator;

import org.openstreetmap.osmosis.core.OsmosisRuntimeException;
import org.openstreetmap.osmosis.core.lifecycle.Completable;
import org.openstreetmap.osmosis.core.lifecycle.ReleasableIterator;
import org.openstreetmap.osmosis.core.sort.common.FileBasedSort;


/**
 * Writes data into an index file and sorts it if input data is unordered. The
 * data must be fixed width to allow index values to be randomly accessed later.
 * 
 * @param <K>
 *            The index key type.
 * @param <T>
 *            The index element type to be stored.
 * @author Brett Henderson
 */
public class IndexStore<K, T extends IndexElement<K>> implements Completable {
	
	private ObjectSerializationFactory serializationFactory;
	private RandomAccessObjectStore<T> indexStore;
	private Comparator<K> ordering;
	private String tempFilePrefix;
	private File indexFile;
	private K previousKey;
	private boolean sorted;
	private long elementCount;
	private long elementSize;
	private boolean complete;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param elementType
	 *            The type of index element to be stored in the index.
	 * @param ordering
	 *            A comparator that sorts index elements desired index key
	 *            ordering.
	 * @param indexFile
	 *            The file to use for storing the index.
	 */
	public IndexStore(Class<T> elementType, Comparator<K> ordering, File indexFile) {
		this.ordering = ordering;
		this.indexFile = indexFile;
		
		serializationFactory = new SingleClassObjectSerializationFactory(elementType);
		
		indexStore = new RandomAccessObjectStore<T>(serializationFactory, indexFile);
		
		sorted = true;
		elementCount = 0;
		elementSize = -1;
		complete = false;
	}
	
	
	/**
	 * Creates a new instance.
	 * 
	 * 
	 * @param elementType
	 *            The type of index element to be stored in the index.
	 * @param ordering
	 *            A comparator that sorts index elements desired index key
	 *            ordering.
	 * @param tempFilePrefix
	 *            The prefix of the temporary file.
	 */
	public IndexStore(Class<T> elementType, Comparator<K> ordering, String tempFilePrefix) {
		this.ordering = ordering;
		this.tempFilePrefix = tempFilePrefix;
		
		serializationFactory = new SingleClassObjectSerializationFactory(elementType);
		
		indexStore = new RandomAccessObjectStore<T>(serializationFactory, tempFilePrefix);
		
		sorted = true;
		elementCount = 0;
		elementSize = -1;
		complete = false;
	}
	
	
	/**
	 * Writes the specified element to the index.
	 * 
	 * @param element
	 *            The index element which includes the identifier when stored.
	 */
	public void write(T element) {
		K key;
		long fileOffset;
		
		if (complete) {
			throw new OsmosisRuntimeException("Cannot write new data once reading has begun.");
		}
		
		fileOffset = indexStore.add(element);
		
		key = element.getKey();
		
		// If the new element contains a key that is not sequential, we need to
		// mark the index as unsorted so we can perform a sort prior to reading.
		if (previousKey != null) {
			if (ordering.compare(previousKey, key) > 0) {
				sorted = false;
			}
		}
		previousKey = key;
		
		elementCount++;
		
		// Calculate and verify the element size. This index requires all keys to be the same length
		// to allow sorting and searching to occur. The first element has a file offset of 0 so we
		// ignore that one. The second element offset will tell us the size of the first element.
		// From that point on we verify that all elements have the same size.
		if (elementCount == 2) {
			elementSize = fileOffset;
		} else if (elementCount > 2) {
			long expectedOffset;
			
			expectedOffset = (elementCount - 1) * elementSize;
			
			if (expectedOffset != fileOffset) {
				throw new OsmosisRuntimeException(
					"Inconsistent element sizes, new file offset=" + fileOffset
					+ ", expected offset=" + expectedOffset
					+ ", element size=" + elementSize
					+ ", element count=" + elementCount
				);
			}
		}
	}
	
	
	/**
	 * Creates a new reader capable of accessing the contents of this store. The
	 * reader must be explicitly released when no longer required. Readers must
	 * be released prior to this store.
	 * 
	 * @return A store reader.
	 */
	public IndexStoreReader<K, T> createReader() {
		return new IndexStoreReader<K, T>(indexStore.createReader(), ordering);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void complete() {
		if (!complete) {
			indexStore.complete();
			
			if (!sorted) {
				final Comparator<K> keyOrdering = ordering;
				
				FileBasedSort<T> fileSort;
				
				// Create a new file based sort instance ordering elements by their
				// identifiers.
				fileSort = new FileBasedSort<T>(
					serializationFactory,
					new Comparator<T>() {
						private Comparator<K> elementKeyOrdering = keyOrdering;
						
						@Override
						public int compare(T o1, T o2) {
							return elementKeyOrdering.compare(o1.getKey(), o2.getKey());
						}
					},
					true
				);
				
				try {
					RandomAccessObjectStoreReader<T> indexStoreReader;
					ReleasableIterator<T> sortIterator;
					
					// Read all data from the index store into the sorting store.
					indexStoreReader = indexStore.createReader();
					try {
						Iterator<T> indexIterator;
						
						indexIterator = indexStoreReader.iterate();
						
						while (indexIterator.hasNext()) {
							fileSort.add(indexIterator.next());
						}
					} finally {
						indexStoreReader.release();
					}
					
					// Release the existing index store and create a new one.
					indexStore.release();
					if (indexFile != null) {
						indexStore = new RandomAccessObjectStore<T>(serializationFactory, indexFile);
					} else {
						indexStore = new RandomAccessObjectStore<T>(serializationFactory, tempFilePrefix);
					}
					
					// Read all data from the sorting store back into the index store.
					sortIterator = fileSort.iterate();
					try {
						while (sortIterator.hasNext()) {
							indexStore.add(sortIterator.next());
						}
					} finally {
						sortIterator.release();
					}
					
				} finally {
					fileSort.release();
				}
			}
			
			complete = true;
		}
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void release() {
		indexStore.release();
	}
}
