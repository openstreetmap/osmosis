// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.dataset.v0_6.impl;

import java.io.File;
import java.util.HashMap;
import java.util.Map;


/**
 * Implements a dataset store file manager using permanent files stored in a specified directory.
 * 
 * @author Brett Henderson
 */
public class PermanentFileDatasetStoreFileManager implements DatasetStoreFileManager {
	
	private File directory;
	private File nodeObjectFile;
	private File nodeObjectOffsetIndexFile;
	private File nodeTileIndexFile;
	private File wayObjectFile;
	private File wayObjectOffsetIndexFile;
	private Map<Integer, File> wayTileIndexFileMap;
	private File nodeWayIndexFile;
	private File relationObjectFile;
	private File relationObjectOffsetIndexFile;
	private File nodeRelationIndexFile;
	private File wayRelationIndexFile;
	private File relationRelationIndexFile;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param directory
	 *            The directory to store all datafiles in.
	 */
	public PermanentFileDatasetStoreFileManager(File directory) {
		this.directory = directory;

		wayTileIndexFileMap = new HashMap<Integer, File>();
	}
	
	
	/**
	 * Creates a file object with the specified file name. It will be located in
	 * the data directory.
	 * 
	 * @param fileName
	 *            The name of the data file.
	 * @return A file object representing the data file.
	 */
	private File createDataFile(String fileName) {
		return new File(directory, fileName);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public File getNodeObjectFile() {
		if (nodeObjectFile == null) {
			nodeObjectFile = createDataFile("dsno");
		}
		
		return nodeObjectFile;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public File getNodeObjectOffsetIndexFile() {
		if (nodeObjectOffsetIndexFile == null) {
			nodeObjectOffsetIndexFile = createDataFile("dsnooi");
		}
		
		return nodeObjectOffsetIndexFile;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public File getNodeTileIndexFile() {
		if (nodeTileIndexFile == null) {
			nodeTileIndexFile = createDataFile("dsnti");
		}
		
		return nodeTileIndexFile;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public File getWayObjectFile() {
		if (wayObjectFile == null) {
			wayObjectFile = createDataFile("dswo");
		}
		
		return wayObjectFile;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public File getWayObjectOffsetIndexFile() {
		if (wayObjectOffsetIndexFile == null) {
			wayObjectOffsetIndexFile = createDataFile("dswooi");
		}
		
		return wayObjectOffsetIndexFile;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public File getWayTileIndexFile(int instance) {
		if (!wayTileIndexFileMap.containsKey(instance)) {
			wayTileIndexFileMap.put(instance, createDataFile("dswti" + instance));
		}
		
		return wayTileIndexFileMap.get(instance);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public File getNodeWayIndexFile() {
		if (nodeWayIndexFile == null) {
			nodeWayIndexFile = createDataFile("dsnwi");
		}
		
		return nodeWayIndexFile;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public File getRelationObjectFile() {
		if (relationObjectFile == null) {
			relationObjectFile = createDataFile("dsro");
		}
		
		return relationObjectFile;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public File getRelationObjectOffsetIndexFile() {
		if (relationObjectOffsetIndexFile == null) {
			relationObjectOffsetIndexFile = createDataFile("dsrooi");
		}
		
		return relationObjectOffsetIndexFile;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public File getNodeRelationIndexFile() {
		if (nodeRelationIndexFile == null) {
			nodeRelationIndexFile = createDataFile("dsnri");
		}
		
		return nodeRelationIndexFile;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public File getWayRelationIndexFile() {
		if (wayRelationIndexFile == null) {
			wayRelationIndexFile = createDataFile("dswri");
		}
		
		return wayRelationIndexFile;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public File getRelationRelationIndexFile() {
		if (relationRelationIndexFile == null) {
			relationRelationIndexFile = createDataFile("dsrri");
		}
		
		return relationRelationIndexFile;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void release() {
		// Do nothing.
	}
}
