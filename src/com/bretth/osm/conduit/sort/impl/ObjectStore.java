package com.bretth.osm.conduit.sort.impl;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import com.bretth.osm.conduit.ConduitRuntimeException;


/**
 * Provides a store for writing objects to a file for later retrieval. The
 * number of objects is limited only by disk space.
 * 
 * @param <DataType>
 *            The object type to be sorted.
 * @author Brett Henderson
 */
public class ObjectStore<DataType> implements Releasable {
	private StorageStage stage;
	private String storageFilePrefix;
	private File file;
	private FileOutputStream fileOutStream;
	private ObjectOutputStream objOutStream;
	private ByteArrayOutputStream arrayOutStream;
	private long fileSize;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param storageFilePrefix
	 */
	public ObjectStore(String storageFilePrefix) {
		this.storageFilePrefix = storageFilePrefix;
		
		stage = StorageStage.NotStarted;
		fileSize = 0;
		
		arrayOutStream = new ByteArrayOutputStream();
		try {
			objOutStream = new ObjectOutputStream(arrayOutStream);
		} catch (IOException e) {
			throw new ConduitRuntimeException("Unable to create object stream.", e);
		}
	}
	
	
	/**
	 * Adds the specified object to the store.
	 * 
	 * @param data
	 *            The object to be added.
	 */
	public void add(DataType data) {
		// We can't add if we've passed the add stage.
		if (stage.compareTo(StorageStage.Add) > 0) {
			throw new ConduitRuntimeException("Cannot add to storage in stage " + stage + ".");
		}
		
		// If we're not up to the add stage, initialise for adding.
		if (stage.compareTo(StorageStage.Add) < 0) {
			try {
				file = File.createTempFile(storageFilePrefix, null);
				
				fileOutStream = new FileOutputStream(file);
				
				stage = StorageStage.Add;
				
			} catch (IOException e) {
				throw new ConduitRuntimeException("Unable to open temporary file " + file + " for writing.", e);
			}
		}
		
		try {
			objOutStream.writeObject(data);
			fileSize += arrayOutStream.size();
			
			arrayOutStream.writeTo(fileOutStream);
			
		} catch (IOException e) {
			throw new ConduitRuntimeException("Unable to write object to file.", e);
		}
	}
	
	
	/**
	 * Returns the size of the underlying storage file.
	 * 
	 * @return The file size.
	 */
	public long getFileSize() {
		return fileSize;
	}
	
	
	/**
	 * Configures the state of this object instance for iterating or reading
	 * mode. If the current state doesn't allow iterating, an exception will be
	 * thrown.
	 * 
	 * @return true if data is available, false otherwise.
	 */
	private boolean initializeIteratingStage() {
		// If we've been released, we can't iterate.
		if (stage.compareTo(StorageStage.Released) >= 0) {
			throw new ConduitRuntimeException("Cannot iterate over storage in stage " + stage + ".");
		}
		
		// If no data was written, an empty iterator should be returned.
		if (stage.compareTo(StorageStage.NotStarted) <= 0) {
			stage = StorageStage.Reading;
			return false;
		}
		
		// If we're in the add stage, close the output stream.
		if (stage.compareTo(StorageStage.Add) == 0) {
			stage = StorageStage.Reading;
			
			try {
				fileOutStream.close();
			} catch (IOException e) {
				throw new ConduitRuntimeException("Unable to close output stream.", e);
			}
			
			fileOutStream = null;
		}
		
		// Data is available.
		return true;
	}
	
	
	/**
	 * Returns an iterator for reading objects from the underlying data store.
	 * 
	 * @return An iterator for reading objects from the data store. This
	 *         iterator must be released after use.
	 */
	public ReleasableIterator<DataType> iterate() {
		return iterate(0, -1);
	}
	
	
	/**
	 * Returns an iterator for reading objects from the underlying data store.
	 * 
	 * @param streamOffset
	 *            The location in the underlying stream to begin reading.
	 * @param maxObjectCount
	 *            The maximum number of objects to be returned, -1 for
	 *            unlimited.
	 * @return An iterator for reading objects from the data store. This
	 *         iterator must be released after use.
	 */
	public ReleasableIterator<DataType> iterate(long streamOffset, long maxObjectCount) {
		FileInputStream fileStream = null;
		
		try {
			ObjectInputStream objStream;
			
			if (!initializeIteratingStage()) {
				return new EmptyIterator<DataType>();
			}
			
			// If we've reached this far, we have a file containing data to be read.  Open a file stream on the file.
			try {
				fileStream = new FileInputStream(file);
			} catch (IOException e) {
				throw new ConduitRuntimeException("Unable to open file for reading.", e);
			}
			
			// Seek to the required starting point in the file.
			if (streamOffset > 0) {
				try {
					fileStream.skip(streamOffset);
				} catch (IOException e) {
					throw new ConduitRuntimeException("Unable to skip to specified location in file.", e);
				}
			}
			
			// Create the object input stream.
			try {
				objStream = new ObjectInputStream(fileStream);
				
			} catch (IOException e) {
				throw new ConduitRuntimeException("Unable to open object stream.", e);
			}
			
			// The stream will be owned by the caller, therefore we must clear
			// the reference now so it isn't closed on method exit.
			fileStream = null;
			
			if (maxObjectCount >= 0) {
				return new SubObjectStreamIterator<DataType>(objStream, maxObjectCount);
			} else {
				return new ObjectStreamIterator<DataType>(objStream);
			}
			
		} finally {
			if (fileStream != null) {
				try {
					fileStream.close();
				} catch (Exception e) {
					// Do nothing.
				}
			}
		}
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void release() {
		if (fileOutStream != null) {
			try {
				fileOutStream.close();
			} catch (Exception e) {
				// Do nothing.
			}
			fileOutStream = null;
		}
		
		if (file != null) {
			file.delete();
			file = null;
		}
		
		stage = StorageStage.Released;
	}
}
