// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package com.bretth.osmosis.core.store;

import java.io.File;
import java.io.FileNotFoundException;
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
public class RandomAccessObjectStore<T extends Storeable> implements Completable {
	private ObjectSerializationFactory serializationFactory;
	private StorageStage stage;
	private String tempFilePrefix;
	private File tempFile;
	private File storageFile;
	private RandomAccessFile randomFile;
	private StoreClassRegister storeClassRegister;
	private ObjectWriter objectWriter;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param serializationFactory
	 *            The factory defining the object serialisation implementation.
	 * @param tempFilePrefix
	 *            The prefix of the temporary file.
	 */
	public RandomAccessObjectStore(ObjectSerializationFactory serializationFactory, String tempFilePrefix) {
		this.serializationFactory = serializationFactory;
		this.tempFilePrefix = tempFilePrefix;
		
		storeClassRegister = new StoreClassRegister();
		
		stage = StorageStage.NotStarted;
	}
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param serializationFactory
	 *            The factory defining the object serialisation implementation.
	 * @param storageFile
	 *            The storage file to use.
	 */
	public RandomAccessObjectStore(ObjectSerializationFactory serializationFactory, File storageFile) {
		this.serializationFactory = serializationFactory;
		this.storageFile = storageFile;
		
		storeClassRegister = new StoreClassRegister();
		
		stage = StorageStage.NotStarted;
	}
	
	
	/**
	 * Initialises the output file and configures the class for adding data.
	 */
	private void initializeAddStage() {
		// We can't add if we've passed the add stage.
		if (stage.compareTo(StorageStage.Add) > 0) {
			throw new OsmosisRuntimeException("Cannot add to storage in stage " + stage + ".");
		}
		
		// If we're not up to the add stage, initialise for adding.
		if (stage.compareTo(StorageStage.Add) < 0) {
			try {
				if (storageFile == null) {
					tempFile = File.createTempFile(tempFilePrefix, null);
					storageFile = tempFile;
				} 
				
				randomFile = new RandomAccessFile(storageFile, "rw");
				
				objectWriter = serializationFactory.createObjectWriter(new DataOutputStoreWriter(randomFile), storeClassRegister);
				
				stage = StorageStage.Add;
				
			} catch (IOException e) {
				throw new OsmosisRuntimeException("Unable to create object stream writing to file " + storageFile + ".", e);
			}
		}
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
		
		initializeAddStage();
		
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
	 */
	private void initializeReadingStage() {
		// If we're already in the reading stage there's nothing to do.
		if (stage.equals(StorageStage.Reading)) {
			return;
		}
		
		// If we haven't reached the reading stage yet, configure for output
		// first to ensure a file is available for reading.
		if (stage.compareTo(StorageStage.Reading) < 0) {
			initializeAddStage();
			
			stage = StorageStage.Reading;
		}
		
		// If we've passed the reading stage, we can't continue.
		if (stage.compareTo(StorageStage.Reading) > 0) {
			throw new OsmosisRuntimeException("Cannot read from storage once we've reached stage " + stage + ".");
		}
	}
	
	
	/**
	 * Creates a new reader capable of accessing the contents of this store. The
	 * reader must be explicitly released when no longer required. Readers must
	 * be released prior to this store.
	 * 
	 * @return A store reader.
	 */
	public RandomAccessObjectStoreReader<T> createReader() {
		initializeReadingStage();
		
		try {
			RandomAccessFile randomFileReader;
			
			randomFileReader = new RandomAccessFile(storageFile, "r");
			
			return new RandomAccessObjectStoreReader<T>(
				randomFileReader,
				serializationFactory.createObjectReader(new StoreReader(randomFileReader), storeClassRegister)
			);
			
		} catch (FileNotFoundException e) {
			throw new OsmosisRuntimeException("Unable to create object stream reading from file " + storageFile + ".", e);
		}
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void complete() {
		initializeReadingStage();
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
		
		if (tempFile != null) {
			tempFile.delete();
			tempFile = null;
		}
		
		stage = StorageStage.Released;
	}
}
