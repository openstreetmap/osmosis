package com.bretth.osmosis.core.customdb;

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
}
