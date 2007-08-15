package com.bretth.osmosis.core.sort.impl;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

import com.bretth.osmosis.core.OsmosisRuntimeException;


/**
 * Manages a file containing a series of long data entities. These long values
 * will be used as indexes into a main data file allowing the main data file to
 * be broken into chunks.
 * 
 * @author Brett Henderson
 */
public class IndexStore implements Releasable {
	private StorageStage stage;
	private String indexFilePrefix;
	private File file;
	private RandomAccessFile randomAccessFile;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param indexFilePrefix
	 *            The prefix of the index file name.
	 */
	public IndexStore(String indexFilePrefix) {
		this.indexFilePrefix = indexFilePrefix;
		stage = StorageStage.NotStarted;
	}
	
	
	/**
	 * Writes the specified value to the specified index.
	 * 
	 * @param index
	 *            The index of the value.
	 * @param value
	 *            The value.
	 */
	public void write(long index, long value) {
		// We can't add if we've passed the add stage.
		if (stage.compareTo(StorageStage.Add) > 0) {
			throw new OsmosisRuntimeException("Cannot add to storage in stage " + stage + ".");
		}
		
		// If we're not up to the add stage, initialise for adding.
		if (stage.compareTo(StorageStage.Add) < 0) {
			try {
				file = File.createTempFile(indexFilePrefix, null);
				
				randomAccessFile = new RandomAccessFile(file, "rw");
				
				stage = StorageStage.Add;
				
			} catch (IOException e) {
				throw new OsmosisRuntimeException("Unable to open temporary file " + file + " for writing.", e);
			}
		}
		
		try {
			// Each long consists of 8 bytes, so we must multiply the index by 8
			// to get the required location.
			randomAccessFile.seek(index * 8);
			randomAccessFile.writeLong(value);
			
		} catch (IOException e) {
			throw new OsmosisRuntimeException("Unable to write object to file.", e);
		}
	}
	
	
	/**
	 * Reads the specified value from the store.
	 * 
	 * @param index
	 *            The index of the value.
	 * @return The value.
	 */
	public long read(long index) {
		// If we've been released, we can't read.
		if (stage.compareTo(StorageStage.Released) >= 0) {
			throw new OsmosisRuntimeException("Cannot iterate over storage in stage " + stage + ".");
		}
		
		stage = StorageStage.Reading;
		
		try {
			// Each long consists of 8 bytes, so we must multiply the index by 8
			// to get the required location.
			randomAccessFile.seek(index * 8);
			
			return randomAccessFile.readLong();
		} catch (IOException e) {
			throw new OsmosisRuntimeException("Unable to read from index " + index + ".", e);
		}
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void release() {
		if (randomAccessFile != null) {
			try {
				randomAccessFile.close();
			} catch (Exception e) {
				// Do nothing.
			}
			randomAccessFile = null;
		}
		
		if (file != null) {
			file.delete();
			file = null;
		}
		
		stage = StorageStage.Released;
	}
}
