package com.bretth.osmosis.core.index;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Comparator;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.bretth.osmosis.core.OsmosisRuntimeException;
import com.bretth.osmosis.core.sort.common.FileBasedSort;
import com.bretth.osmosis.core.store.EndOfStoreException;
import com.bretth.osmosis.core.store.ObjectSerializationFactory;
import com.bretth.osmosis.core.store.Releasable;
import com.bretth.osmosis.core.store.ReleasableIterator;
import com.bretth.osmosis.core.store.SingleClassObjectSerializationFactory;
import com.bretth.osmosis.core.store.StorageStage;
import com.bretth.osmosis.core.store.StoreReader;
import com.bretth.osmosis.core.store.StoreWriter;


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
	
	private StorageStage stage;
	private File indexFile;
	private ObjectSerializationFactory serializationFactory;
	private IndexElementFactory<T> elementFactory;
	private DataOutputStream outputStream;
	private DataInputStream inputStream;
	private StoreWriter storeWriter;
	private StoreReader storeReader;
	private long previousId;
	private boolean sorted;
	
	
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
	public IndexWriter(File indexFile, IndexElementFactory<T> elementFactory, Class<T> elementType) {
		this.indexFile = indexFile;
		this.elementFactory = elementFactory;
		
		serializationFactory = new SingleClassObjectSerializationFactory(elementType);
		
		stage = StorageStage.NotStarted;
		
		previousId = Long.MIN_VALUE;
		sorted = true;
	}
	
	
	private void openIndexFileForWriting() {
		if (outputStream != null) {
			throw new OsmosisRuntimeException("Can't open index file for writing because it is already open.");
		}
		if (inputStream != null) {
			throw new OsmosisRuntimeException("Can't open index file for writing because it is already open for reading.");
		}
		
		try {
			outputStream = new DataOutputStream(new FileOutputStream(indexFile));
		} catch (FileNotFoundException e) {
			throw new OsmosisRuntimeException("Unable to open file " + indexFile + " for writing.", e);
		}
		
		storeWriter = new StoreWriter(outputStream);
	}
	
	
	private void closeIndexFileForWriting() {
		if (outputStream == null) {
			throw new OsmosisRuntimeException("Can't close index file for writing because it is already closed.");
		}
		
		try {
			outputStream.close();
		} catch (IOException e) {
			throw new OsmosisRuntimeException("Unable to close index file " + indexFile + ".", e);
		}
		
		outputStream = null;
		storeWriter = null;
	}
	
	
	private void openIndexFileForReading() {
		if (inputStream != null) {
			throw new OsmosisRuntimeException("Can't open index file for reading because it is already open.");
		}
		if (outputStream != null) {
			throw new OsmosisRuntimeException("Can't open index file for reading because it is already open for writing.");
		}
		
		try {
			inputStream = new DataInputStream(new FileInputStream(indexFile));
		} catch (FileNotFoundException e) {
			throw new OsmosisRuntimeException("Unable to open file " + indexFile + " for reading.", e);
		}
		
		storeReader = new StoreReader(inputStream);
	}
	
	
	private void closeIndexFileForReading() {
		if (inputStream == null) {
			throw new OsmosisRuntimeException("Can't close index file for reading because it is already closed.");
		}
		
		try {
			inputStream.close();
		} catch (IOException e) {
			throw new OsmosisRuntimeException("Unable to close index file " + indexFile + ".", e);
		}
		
		inputStream = null;
		storeReader = null;
	}
	
	
	/**
	 * Initialises the index file for writing. Must be called by sub-classes
	 * prior to writing a new element to the output stream.
	 */
	protected void initialize() {
		// We can't initialise the add stage if we're already past it.
		if (stage.compareTo(StorageStage.Add) > 0) {
			throw new OsmosisRuntimeException("Cannot write to the index once writing has completed, current stage=" + stage + ".");
		}
		
		// Only initialise writing if we haven't started yet.
		if (stage.compareTo(StorageStage.Add) < 0) {
			openIndexFileForWriting();
			
			stage = StorageStage.Add;
		}
	}
	
	
	/**
	 * Writes the specified element to the index.
	 * 
	 * @param element
	 *            The index element which includes the identifier when stored.
	 */
	public void write(T element) {
		long id;
		
		initialize();
		
		elementFactory.storeElement(storeWriter, element);
		
		id = element.getId();
		if (previousId > id) {
			sorted = false;
		}
		previousId = id;
	}
	
	
	/**
	 * Finishes all file writes and sorts the file contents if necessary.
	 */
	public void complete() {
		initialize();
		
		closeIndexFileForWriting();
		
		if (!sorted) {
			FileBasedSort<T> fileSort;
			
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
				ReleasableIterator<T> sortedDataIterator;
				
				openIndexFileForReading();
				
				while (true) {
					T element;
					
					element = elementFactory.loadElement(storeReader);
					
					try {
						element = elementFactory.loadElement(storeReader);
						
					} catch (EndOfStoreException e) {
						break;
					}
					
					fileSort.add(element);
				}
				
				closeIndexFileForReading();
				openIndexFileForWriting();
				
				sortedDataIterator = fileSort.iterate();
				try {
					while (sortedDataIterator.hasNext()) {
						T element;
						
						element = sortedDataIterator.next();
						
						elementFactory.storeElement(storeWriter, element);
					}
					
				} finally {
					sortedDataIterator.release();
				}
				
				closeIndexFileForWriting();
				
			} finally {
				fileSort.release();
			}
		}
		
		stage = StorageStage.Reading;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void release() {
		if (outputStream != null) {
			try {
				outputStream.close();
			} catch (Exception e) {
				log.log(Level.SEVERE, "Unable to close index file " + indexFile + " for writing.", e);
			}
			
			outputStream = null;
		}
		
		if (inputStream != null) {
			try {
				inputStream.close();
			} catch (Exception e) {
				log.log(Level.SEVERE, "Unable to close index file " + indexFile + " for reading.", e);
			}
			
			inputStream = null;
		}
		
		stage = StorageStage.Released;
	}
}
