// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.store;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPOutputStream;

import org.openstreetmap.osmosis.core.OsmosisRuntimeException;
import org.openstreetmap.osmosis.core.lifecycle.Completable;
import org.openstreetmap.osmosis.core.lifecycle.ReleasableIterator;
import org.openstreetmap.osmosis.core.util.MultiMemberGZIPInputStream;


/**
 * Provides a store for writing objects to a file for later retrieval. The
 * number of objects is limited only by disk space.
 * 
 * @param <T>
 *            The object type to be stored.
 * @author Brett Henderson
 */
public class SimpleObjectStore<T extends Storeable> implements Completable {
	
	private static final Logger LOG = Logger.getLogger(SimpleObjectStore.class.getName());
	
	private ObjectSerializationFactory serializationFactory;
	private StorageStage stage;
	private String storageFilePrefix;
	private File file;
	private FileOutputStream fileOutStream;
	private DataOutputStream dataOutStream;
	private StoreClassRegister storeClassRegister;
	private ObjectWriter objectWriter;
	private boolean useCompression;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param serializationFactory
	 *            The factory defining the object serialisation implementation.
	 * @param storageFilePrefix
	 *            The prefix of the storage file.
	 * @param useCompression
	 *            If true, the storage file will be compressed.
	 */
	public SimpleObjectStore(
			ObjectSerializationFactory serializationFactory, String storageFilePrefix, boolean useCompression) {
		this.serializationFactory = serializationFactory;
		this.storageFilePrefix = storageFilePrefix;
		this.useCompression = useCompression;
		
		storeClassRegister = new DynamicStoreClassRegister();
		
		stage = StorageStage.NotStarted;
	}
	
	
	/**
	 * Adds the specified object to the store.
	 * 
	 * @param data
	 *            The object to be added.
	 */
	public void add(T data) {
		// We can't add if we've passed the add stage.
		if (stage.compareTo(StorageStage.Add) > 0) {
			throw new OsmosisRuntimeException("Cannot add to storage in stage " + stage + ".");
		}
		
		// If we're not up to the add stage, initialise for adding.
		if (stage.compareTo(StorageStage.Add) < 0) {
			try {
				file = File.createTempFile(storageFilePrefix, null);
				
				fileOutStream = new FileOutputStream(file);
				
				if (useCompression) {
					dataOutStream = new DataOutputStream(
							new BufferedOutputStream(new GZIPOutputStream(fileOutStream), 65536));
				} else {
					dataOutStream = new DataOutputStream(
							new BufferedOutputStream(fileOutStream, 65536));
				}
				
				objectWriter = serializationFactory.createObjectWriter(
						new DataOutputStoreWriter(dataOutStream), storeClassRegister);
				
				stage = StorageStage.Add;
				
			} catch (IOException e) {
				throw new OsmosisRuntimeException(
						"Unable to create object stream writing to temporary file " + file + ".", e);
			}
		}
		
		// Write the object to the store.
		objectWriter.writeObject(data);
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
			throw new OsmosisRuntimeException("Cannot iterate over storage in stage " + stage + ".");
		}
		
		// If no data was written, an empty iterator should be returned.
		if (stage.compareTo(StorageStage.NotStarted) <= 0) {
			return false;
		}
		
		// If we're in the add stage, close the output streams.
		if (stage.compareTo(StorageStage.Add) == 0) {
			try {
				dataOutStream.close();
				fileOutStream.close();
				
			} catch (IOException e) {
				throw new OsmosisRuntimeException("Unable to close output stream.", e);
			} finally {
				dataOutStream = null;
				fileOutStream = null;
			}
			
			stage = StorageStage.Reading;
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
	public ReleasableIterator<T> iterate() {
		FileInputStream fileStream = null;
		
		try {
			DataInputStream dataInStream;
			
			if (!initializeIteratingStage()) {
				return new EmptyIterator<T>();
			}
			
			// If we've reached this far, we have a file containing data to be read.  Open a file stream on the file.
			try {
				fileStream = new FileInputStream(file);
			} catch (IOException e) {
				throw new OsmosisRuntimeException("Unable to open file for reading.", e);
			}
			
			// Create the object input stream.
			try {
				if (useCompression) {
					dataInStream = new DataInputStream(
							new BufferedInputStream(new MultiMemberGZIPInputStream(fileStream), 65536));
				} else {
					dataInStream = new DataInputStream(
							new BufferedInputStream(fileStream, 65536));
				}
				
			} catch (IOException e) {
				throw new OsmosisRuntimeException("Unable to open object stream.", e);
			}
			
			// The stream will be owned by the caller, therefore we must clear
			// the reference now so it isn't closed on method exit.
			fileStream = null;
			
			return new ObjectStreamIterator<T>(
					dataInStream,
					serializationFactory.createObjectReader(
							new DataInputStoreReader(dataInStream), storeClassRegister));
			
		} finally {
			if (fileStream != null) {
				try {
					fileStream.close();
				} catch (IOException e) {
					// We are already in an error condition so log and continue.
					LOG.log(Level.WARNING, "Unable to close file input stream.", e);
				}
			}
		}
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void complete() {
		// Do nothing.
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void release() {
		if (fileOutStream != null) {
			try {
				fileOutStream.close();
			} catch (IOException e) {
				// We cannot throw an exception within a release statement.
				LOG.log(Level.WARNING, "Unable to close file output stream.", e);
			}
			fileOutStream = null;
		}
		
		if (file != null) {
			if (!file.delete()) {
				LOG.warning("Unable to delete file " + file);
			}
			file = null;
		}
		
		stage = StorageStage.Released;
	}
}
