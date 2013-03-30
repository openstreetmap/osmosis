// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.dataset.v0_6.impl;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.openstreetmap.osmosis.core.OsmosisRuntimeException;


/**
 * Implements a dataset store file manager using temporary files.
 * 
 * @author Brett Henderson
 */
public class TempFileDatasetStoreFileManager implements DatasetStoreFileManager {
	
	private static Logger log = Logger.getLogger(TempFileDatasetStoreFileManager.class.getName());
	
	
	private List<File> allFiles;
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
	 */
	public TempFileDatasetStoreFileManager() {
		allFiles = new ArrayList<File>();
		wayTileIndexFileMap = new HashMap<Integer, File>();
	}
	
	
	/**
	 * Creates a temporary file.
	 * 
	 * @param prefix
	 *            The temporary file name prefix.
	 * @return The newly created temporary file.
	 */
	private File createTempFile(String prefix) {
		try {
			File file;
			
			file = File.createTempFile(prefix, null);
			
			allFiles.add(file);
			
			return file;
			
		} catch (IOException e) {
			throw new OsmosisRuntimeException("Unable to create a new temporary file.", e);
		}
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public File getNodeObjectFile() {
		if (nodeObjectFile == null) {
			nodeObjectFile = createTempFile("dsno");
		}
		
		return nodeObjectFile;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public File getNodeObjectOffsetIndexFile() {
		if (nodeObjectOffsetIndexFile == null) {
			nodeObjectOffsetIndexFile = createTempFile("dsnooi");
		}
		
		return nodeObjectOffsetIndexFile;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public File getNodeTileIndexFile() {
		if (nodeTileIndexFile == null) {
			nodeTileIndexFile = createTempFile("dsnti");
		}
		
		return nodeTileIndexFile;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public File getWayObjectFile() {
		if (wayObjectFile == null) {
			wayObjectFile = createTempFile("dswo");
		}
		
		return wayObjectFile;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public File getWayObjectOffsetIndexFile() {
		if (wayObjectOffsetIndexFile == null) {
			wayObjectOffsetIndexFile = createTempFile("dswooi");
		}
		
		return wayObjectOffsetIndexFile;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public File getWayTileIndexFile(int instance) {
		if (!wayTileIndexFileMap.containsKey(instance)) {
			wayTileIndexFileMap.put(instance, createTempFile("dswti"));
		}
		
		return wayTileIndexFileMap.get(instance);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public File getNodeWayIndexFile() {
		if (nodeWayIndexFile == null) {
			nodeWayIndexFile = createTempFile("dsnwi");
		}
		
		return nodeWayIndexFile;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public File getRelationObjectFile() {
		if (relationObjectFile == null) {
			relationObjectFile = createTempFile("dsro");
		}
		
		return relationObjectFile;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public File getRelationObjectOffsetIndexFile() {
		if (relationObjectOffsetIndexFile == null) {
			relationObjectOffsetIndexFile = createTempFile("dsrooi");
		}
		
		return relationObjectOffsetIndexFile;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public File getNodeRelationIndexFile() {
		if (nodeRelationIndexFile == null) {
			nodeRelationIndexFile = createTempFile("dsnri");
		}
		
		return nodeRelationIndexFile;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public File getWayRelationIndexFile() {
		if (wayRelationIndexFile == null) {
			wayRelationIndexFile = createTempFile("dswri");
		}
		
		return wayRelationIndexFile;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public File getRelationRelationIndexFile() {
		if (relationRelationIndexFile == null) {
			relationRelationIndexFile = createTempFile("dsrri");
		}
		
		return relationRelationIndexFile;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void release() {
		for (File file : allFiles) {
			if (!file.delete()) {
				log.warning("Unable to delete file " + file);
			}
		}
	}
}
