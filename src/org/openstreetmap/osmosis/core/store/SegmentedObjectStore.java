// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.store;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
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
 * <p>
 * This class supports chunking where the stream is broken into segments. This
 * is achieved by calling the closeChunk method between add calls.
 * <p>
 * This store is only suitable for single-threaded use because it does not
 * provide per-thread readers.
 * 
 * @param <T>
 *            The object type to be stored.
 * @author Brett Henderson
 */
public class SegmentedObjectStore<T extends Storeable> implements Completable {
	
	private static final Logger LOG = Logger.getLogger(SegmentedObjectStore.class.getName());
	
	private ObjectSerializationFactory serializationFactory;
	private StorageStage stage;
	private String storageFilePrefix;
	private File file;
	private FileOutputStream fileOutStream;
	private DataOutputStream dataOutStream;
	private ByteArrayOutputStream arrayOutStream;
	private StoreClassRegister storeClassRegister;
	private ObjectWriter objectWriter;
	private boolean chunkActive; 
	private boolean useCompression;
	private long fileSize;
	
	
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
	public SegmentedObjectStore(
			ObjectSerializationFactory serializationFactory, String storageFilePrefix, boolean useCompression) {
		this.serializationFactory = serializationFactory;
		this.storageFilePrefix = storageFilePrefix;
		this.useCompression = useCompression;
		
		storeClassRegister = new DynamicStoreClassRegister();
		
		stage = StorageStage.NotStarted;
		fileSize = 0;
		
		chunkActive = false;
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
				
				stage = StorageStage.Add;
				
			} catch (IOException e) {
				throw new OsmosisRuntimeException("Unable to open temporary file " + file + " for writing.", e);
			}
		}
		
		// Initialise the current chunk if it isn't already.
		if (!chunkActive) {
			try {
				arrayOutStream = new ByteArrayOutputStream();
				
				if (useCompression) {
					dataOutStream = new DataOutputStream(
							new BufferedOutputStream(
									new GZIPOutputStream(arrayOutStream), 65536));
				} else {
					dataOutStream = new DataOutputStream(new BufferedOutputStream(arrayOutStream, 65536));
				}
				
				objectWriter = serializationFactory.createObjectWriter(
						new DataOutputStoreWriter(dataOutStream), storeClassRegister);
				
				chunkActive = true;
				
			} catch (IOException e) {
				throw new OsmosisRuntimeException("Unable to create object stream.", e);
			}
		}
		
		// Write the object to the store.
		objectWriter.writeObject(data);
		
		// Update the file position based on the buffer size.
		fileSize += arrayOutStream.size();
		
		// Write the buffer to file, and clear the buffer.
		try {
			arrayOutStream.writeTo(fileOutStream);
			arrayOutStream.reset();
			
		} catch (IOException e) {
			throw new OsmosisRuntimeException("Unable to write object to file.", e);
		}
	}
	
	
	/**
	 * Closes the current object stream and creates a new one. This allows read
	 * operations to begin at offsets within the file. This can only be called
	 * while adding to the store, not once reads are begun. Read operations must
	 * begin at offsets created by this method.
	 * 
	 * @return The start position of the new chunk within the file.
	 */
	public long closeChunk() {
		// We can only create an interval if we are in add mode.
		if (stage.compareTo(StorageStage.Add) != 0) {
			throw new OsmosisRuntimeException("Cannot create interval in stage " + stage + ".");
		}
		
		// Nothing needs to be done if the chunk is not yet active.
		if (chunkActive) {
			try {
				dataOutStream.close();
				fileSize += arrayOutStream.size();
				
				arrayOutStream.writeTo(fileOutStream);
				arrayOutStream.reset();
				
				// Subsequent writes must begin a new object stream.
				arrayOutStream = null;
				dataOutStream = null;
				
				chunkActive = false;
				
			} catch (IOException e) {
				throw new OsmosisRuntimeException("Unable to create a new interval.", e);
			}
		}
		
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
			throw new OsmosisRuntimeException("Cannot iterate over storage in stage " + stage + ".");
		}
		
		// If no data was written, an empty iterator should be returned.
		if (stage.compareTo(StorageStage.NotStarted) <= 0) {
			return false;
		}
		
		// If we're in the add stage, close the current chunk and overall file stream.
		if (stage.compareTo(StorageStage.Add) == 0) {
			closeChunk();
			
			try {
				fileOutStream.close();
				
			} catch (IOException e) {
				throw new OsmosisRuntimeException("Unable to close output stream.", e);
			} finally {
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
	public ReleasableIterator<T> iterate(long streamOffset, long maxObjectCount) {
		FileInputStream fileStream = null;
		
		try {
			DataInputStream dataInStream;
			ObjectReader objectReader;
			
			if (!initializeIteratingStage()) {
				return new EmptyIterator<T>();
			}
			
			// If we've reached this far, we have a file containing data to be read.  Open a file stream on the file.
			try {
				fileStream = new FileInputStream(file);
			} catch (IOException e) {
				throw new OsmosisRuntimeException("Unable to open file for reading.", e);
			}
			
			// Seek to the required starting point in the file.
			if (streamOffset > 0) {
				try {
					fileStream.skip(streamOffset);
				} catch (IOException e) {
					throw new OsmosisRuntimeException("Unable to skip to specified location in file.", e);
				}
			}
			
			// Create the object input stream.
			try {
				if (useCompression) {
					dataInStream = new DataInputStream(
							new BufferedInputStream(
									new MultiMemberGZIPInputStream(fileStream), 65536));
				} else {
					dataInStream = new DataInputStream(new BufferedInputStream(fileStream, 65536));
				}
				
			} catch (IOException e) {
				throw new OsmosisRuntimeException("Unable to open object stream.", e);
			}
			
			// The stream will be owned by the caller, therefore we must clear
			// the reference now so it isn't closed on method exit.
			fileStream = null;
			
			objectReader = serializationFactory.createObjectReader(
					new DataInputStoreReader(dataInStream), storeClassRegister);
			
			if (maxObjectCount >= 0) {
				return new SubObjectStreamIterator<T>(dataInStream, objectReader, maxObjectCount);
			} else {
				return new ObjectStreamIterator<T>(dataInStream, objectReader);
			}
			
		} finally {
			if (fileStream != null) {
				try {
					fileStream.close();
				} catch (IOException e) {
					// We are already in an error condition so log and continue.
					LOG.log(Level.WARNING, "Unable to close result set.", e);
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
			} catch (Exception e) {
				// We cannot throw an exception within a release statement.
				LOG.log(Level.WARNING, "Unable to file output stream.", e);
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
