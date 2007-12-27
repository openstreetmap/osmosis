package com.bretth.osmosis.core.store;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

import com.bretth.osmosis.core.OsmosisRuntimeException;


/**
 * Provides a store for writing objects to a file for later retrieval. The
 * number of objects is limited only by disk space.
 * 
 * @param <T>
 *            The object type to be stored.
 * @author Brett Henderson
 */
public class RandomAccessObjectStore<T extends Storeable> implements Releasable {
	private StorageStage stage;
	private String storageFilePrefix;
	private File file;
	private RandomAccessFile randomFile;
	private StoreClassRegister storeClassRegister;
	private GenericObjectWriter objectWriter;
	private GenericObjectReader objectReader;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param storageFilePrefix
	 *            The prefix of the storage file.
	 */
	public RandomAccessObjectStore(String storageFilePrefix) {
		this.storageFilePrefix = storageFilePrefix;
		
		storeClassRegister = new StoreClassRegister();
		
		stage = StorageStage.NotStarted;
	}
	
	
	/**
	 * Adds the specified object to the store.
	 * 
	 * @param data
	 *            The object to be added.
	 * @return The offset within the output file of the object written.
	 */
	public long add(T data) {
		long objectFileOffset;
		
		// We can't add if we've passed the add stage.
		if (stage.compareTo(StorageStage.Add) > 0) {
			throw new OsmosisRuntimeException("Cannot add to storage in stage " + stage + ".");
		}
		
		// If we're not up to the add stage, initialise for adding.
		if (stage.compareTo(StorageStage.Add) < 0) {
			try {
				file = File.createTempFile(storageFilePrefix, null);
				
				randomFile = new RandomAccessFile(file, "rw");
				
				objectWriter = new GenericObjectWriter(new StoreWriter(randomFile), storeClassRegister);
				objectReader = new GenericObjectReader(new StoreReader(randomFile), storeClassRegister);
				
				stage = StorageStage.Add;
				
			} catch (IOException e) {
				throw new OsmosisRuntimeException("Unable to create object stream writing to temporary file " + file + ".", e);
			}
		}
		
		try {
			objectFileOffset = randomFile.getFilePointer();
		} catch (IOException e) {
			throw new OsmosisRuntimeException("Unable to obtain the current file offset.", e);
		}
		
		// Write the object to the store.
		objectWriter.writeObject(data);
		
		return objectFileOffset;
	}
	
	
	/**
	 * Configures the state of this object instance for reading mode. If the
	 * current state doesn't allow reading, an exception will be thrown.
	 * 
	 * @return true if data is available, false otherwise.
	 */
	private boolean initializeReadingStage() {
		// If we're already in the reading stage there's nothing to do.
		if (stage.equals(StorageStage.Reading)) {
			return true;
		}
		
		// If we've been released, we can't iterate.
		if (stage.compareTo(StorageStage.Released) >= 0) {
			throw new OsmosisRuntimeException("Cannot iterate over storage in stage " + stage + ".");
		}
		
		// If no data was written, an empty iterator should be returned.
		if (stage.compareTo(StorageStage.NotStarted) <= 0) {
			return false;
		}
		
		// If we're in the add stage, close the output streams.
		if (stage.equals(StorageStage.Add)) {
			stage = StorageStage.Reading;
			
			return true;
		}
		
		throw new OsmosisRuntimeException("The storage stage " + stage + " is not recognised.");
	}
	
	
	/**
	 * Reads the object at the specified file offset.
	 * 
	 * @param offset
	 *            The file offset to read an object from.
	 * @return The requested object.
	 */
	@SuppressWarnings("unchecked")
	public T get(long offset) {
		if (!initializeReadingStage()) {
			throw new OsmosisRuntimeException("No data is available for reading.");
		}
		
		try {
			randomFile.seek(offset);
		} catch (IOException e) {
			throw new OsmosisRuntimeException("Unable to seek to position " + offset + " in temp file " + file + ".");
		}
		
		return (T) objectReader.readObject();
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void release() {
		if (randomFile != null) {
			try {
				randomFile.close();
			} catch (Exception e) {
				// Do nothing.
			}
			randomFile = null;
		}
		
		if (file != null) {
			file.delete();
			file = null;
		}
		
		stage = StorageStage.Released;
	}
}
