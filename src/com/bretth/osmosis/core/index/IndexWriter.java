package com.bretth.osmosis.core.index;

import java.io.File;
import java.util.Comparator;
import java.util.Iterator;
import java.util.logging.Logger;

import com.bretth.osmosis.core.OsmosisRuntimeException;
import com.bretth.osmosis.core.sort.common.FileBasedSort;
import com.bretth.osmosis.core.store.ObjectSerializationFactory;
import com.bretth.osmosis.core.store.RandomAccessObjectStore;
import com.bretth.osmosis.core.store.RandomAccessObjectStoreReader;
import com.bretth.osmosis.core.store.Releasable;
import com.bretth.osmosis.core.store.ReleasableIterator;
import com.bretth.osmosis.core.store.SingleClassObjectSerializationFactory;


/**
 * Writes data into an index file and sorts it if input data is unordered. The
 * data must be fixed width to allow index values to be randomly accessed later.
 * 
 * @param <T>
 *            The index element type to be stored.
 * @author Brett Henderson
 */
public class IndexWriter<T extends IndexElement> implements Releasable {
	static final Logger log = Logger.getLogger(IndexWriter.class.getName());
	
	private ObjectSerializationFactory serializationFactory;
	private RandomAccessObjectStore<T> indexStore;
	private File indexFile;
	private long previousId;
	private boolean sorted;
	private long elementCount;
	private long elementSize;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param indexFile
	 *            The file to use for storing the index.
	 * @param elementFactory
	 *            The factory for persisting and loading element data.
	 * @param elementType
	 *            The type of index element to be stored in the index.
	 */
	public IndexWriter(File indexFile, Class<T> elementType) {
		this.indexFile = indexFile;
		
		serializationFactory = new SingleClassObjectSerializationFactory(elementType);
		
		indexStore = new RandomAccessObjectStore<T>(serializationFactory, indexFile);
		
		previousId = Long.MIN_VALUE;
		sorted = true;
		elementCount = 0;
		elementSize = -1;
	}
	
	
	/**
	 * Writes the specified element to the index.
	 * 
	 * @param element
	 *            The index element which includes the identifier when stored.
	 */
	public void write(T element) {
		long id;
		long fileOffset;
		
		fileOffset = indexStore.add(element);
		
		id = element.getId();
		if (previousId > id) {
			sorted = false;
		}
		previousId = id;
		
		elementCount++;
		
		// Calculate and verify the element size.
		if (elementCount == 1) {
			elementSize = fileOffset;
		} else {
			long expectedOffset;
			
			expectedOffset = elementCount * elementSize;
			
			if (expectedOffset != fileOffset) {
				throw new OsmosisRuntimeException(
					"Inconsistent element sizes, new file offset=" + fileOffset
					+ ", expected offset=" + expectedOffset
					+ ", element size="+ elementSize
					+ ", element count=" + elementCount
				);
			}
		}
	}
	
	
	/**
	 * Finishes all file writes and sorts the file contents if necessary.
	 */
	public void complete() {
		if (!sorted) {
			FileBasedSort<T> fileSort;
			
			// Create a new file based sort instance ordering elements by their
			// identifiers.
			fileSort = new FileBasedSort<T>(
				serializationFactory,
				new Comparator<T>() {
					
					@Override
					public int compare(T o1, T o2) {
						long result;
						
						result = o1.getId() - o2.getId();
						
						if (result == 0) {
							return 0;
						} else if (result < 0) {
							return -1;
						} else {
							return 1;
						}
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
				indexStore = new RandomAccessObjectStore<T>(serializationFactory, indexFile);
				
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
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void release() {
		indexStore.release();
	}
}
