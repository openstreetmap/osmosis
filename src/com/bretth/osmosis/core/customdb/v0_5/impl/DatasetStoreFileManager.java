// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package com.bretth.osmosis.core.customdb.v0_5.impl;

import java.io.File;

import com.bretth.osmosis.core.store.Releasable;


/**
 * Defines the operations required for a manager of dataset store files.
 * 
 * @author Brett Henderson
 */
public interface DatasetStoreFileManager extends Releasable {
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
	 * Returns the file to be used for storing way objects.
	 * 
	 * @return The way object file.
	 */
	public File getWayObjectFile();
	
	
	/**
	 * Returns the file to be used for storing way object offsets against their
	 * id.
	 * 
	 * @return The way object offset index file.
	 */
	public File getWayObjectOffsetIndexFile();
	
	
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
	
	
	/**
	 * Returns the file to be used for storing relationships between nodes and
	 * ways.
	 * 
	 * @return The node way index file.
	 */
	public File getNodeWayIndexFile();
	
	
	/**
	 * Returns the file to be used for storing relation objects.
	 * 
	 * @return The relation object file.
	 */
	public File getRelationObjectFile();
	
	
	/**
	 * Returns the file to be used for storing relation object offsets against
	 * their id.
	 * 
	 * @return The relation object offset index file.
	 */
	public File getRelationObjectOffsetIndexFile();
	
	
	/**
	 * Returns the file to be used for storing relationships between nodes and
	 * relations.
	 * 
	 * @return The node relation index file.
	 */
	public File getNodeRelationIndexFile();
	
	
	/**
	 * Returns the file to be used for storing relationships between ways and
	 * relations.
	 * 
	 * @return The way relation index file.
	 */
	public File getWayRelationIndexFile();
	
	
	/**
	 * Returns the file to be used for storing relationships between relations
	 * and relations.
	 * 
	 * @return The relation relation index file.
	 */
	public File getRelationRelationIndexFile();
}
