// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.store;

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.openstreetmap.osmosis.core.OsmosisRuntimeException;
import org.openstreetmap.osmosis.core.lifecycle.Completable;


/**
 * Provides a store for writing objects to a file for later retrieval. The
 * number of objects is limited only by disk space.
 * 
 * @param <T>
 *            The object type to be stored.
 * @author Brett Henderson
 */
public class RandomAccessObjectStore<T extends Storeable> implements Completable {
	
	private static final Logger LOG = Logger.getLogger(RandomAccessObjectStore.class.getName());
	
	private ObjectSerializationFactory serializationFactory;
	private StorageStage stage;
	private String tempFilePrefix;
	private File tempFile;
	private File storageFile;
	private OffsetTrackingOutputStream offsetTrackingStream;
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
		
		storeClassRegister = new DynamicStoreClassRegister();
		
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
		
		storeClassRegister = new DynamicStoreClassRegister();
		
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
			FileOutputStream fileStream = null;
			try {
				if (storageFile == null) {
					tempFile = File.createTempFile(tempFilePrefix, null);
					storageFile = tempFile;
				} 
				
				fileStream = new FileOutputStream(storageFile);
				offsetTrackingStream = new OffsetTrackingOutputStream(new BufferedOutputStream(fileStream, 65536));
				
				// Clear reference so that the stream doesn't get closed at the end of this method.
				fileStream = null;
				
				objectWriter = serializationFactory.createObjectWriter(
					new DataOutputStoreWriter(new DataOutputStream(offsetTrackingStream)),
					storeClassRegister
				);
				
				stage = StorageStage.Add;
				
			} catch (IOException e) {
				throw new OsmosisRuntimeException(
						"Unable to create object stream writing to file " + storageFile + ".", e);
			} finally {
				if (fileStream != null) {
					try {
						fileStream.close();
					} catch (IOException e) {
						// We are already in an error condition so log and continue.
						LOG.log(Level.WARNING, "Unable to close file stream.", e);
					}
				}
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
		
		objectFileOffset = offsetTrackingStream.getByteCount();
		
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
		
		if (stage.equals(StorageStage.Add)) {
			throw new OsmosisRuntimeException(
					"Cannot begin reading in " + StorageStage.Add + " stage, must call complete first.");
		}
		
		// If we haven't reached the reading stage yet, configure for output
		// first to ensure a file is available for reading.
		if (stage.compareTo(StorageStage.Reading) < 0) {
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
			BufferedRandomAccessFileInputStream randomFileReader;
			
			randomFileReader = new BufferedRandomAccessFileInputStream(storageFile);
			
			return new RandomAccessObjectStoreReader<T>(
				randomFileReader,
				serializationFactory.createObjectReader(
						new DataInputStoreReader(
								new DataInputStream(randomFileReader)), storeClassRegister)
			);
			
		} catch (FileNotFoundException e) {
			throw new OsmosisRuntimeException(
					"Unable to create object stream reading from file " + storageFile + ".", e);
		}
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void complete() {
		// If we're already in the reading stage, we don't need to perform a
		// complete.
		if (stage.compareTo(StorageStage.Reading) != 0) {
			// We need to make sure we pass through the add stage to ensure an
			// output file is created.
			initializeAddStage();
			
			try {
				offsetTrackingStream.close();
				offsetTrackingStream = null;
				
			} catch (IOException e) {
				throw new OsmosisRuntimeException("Unable to close the file " + storageFile + ".");
			}
			
			stage = StorageStage.Reading;
		}
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void release() {
		if (offsetTrackingStream != null) {
			try {
				offsetTrackingStream.close();
			} catch (Exception e) {
				// We cannot throw an exception within a release statement.
				LOG.log(Level.WARNING, "Unable to close offset tracking output stream.", e);
			}
			offsetTrackingStream = null;
		}
		
		if (tempFile != null) {
			if (!tempFile.delete()) {
				// We cannot throw an exception within a release statement.
				LOG.warning("Unable to delete file " + tempFile);
			}
			tempFile = null;
		}
		
		stage = StorageStage.Released;
	}
}
