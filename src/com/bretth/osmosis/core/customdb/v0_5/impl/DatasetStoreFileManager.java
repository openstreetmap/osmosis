package com.bretth.osmosis.core.customdb.v0_5.impl;

import java.io.File;


/**
 * Defines the operations required for a manager of dataset store files.
 * 
 * @author Brett Henderson
 */
public interface DatasetStoreFileManager {
	/**
	 * Returns the file to be used for storing node objects.
	 * 
	 * @return The node object file.
	 */
	public File getNodeObjectFile();
	
	
	/**
	 * Returns the file to be used for storing node object offsets against their
	 * id.
	 * 
	 * @return The node object offset index file.
	 */
	public File getNodeObjectOffsetIndexFile();
	
	
	/**
	 * Returns the file to be used for storing node ids against tile ids.
	 * 
	 * @return The node tile index file.
	 */
	public File getNodeTileIndexFile();
	
	
	/**
	 * Returns the file to be used for storing way ids against tile ids. Because
	 * multiple files are used for storing way tile indexes, the implementation
	 * must support an arbitrary number of index files to be returned.
	 * 
	 * @param instance
	 *            The index file number.
	 * @return The way tile index file.
	 */
	public File getWayTileIndexFile(int instance);
}
