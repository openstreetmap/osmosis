// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.pgsimple.v0_6.impl;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Logger;

import org.openstreetmap.osmosis.core.OsmosisRuntimeException;
import org.openstreetmap.osmosis.core.lifecycle.Releasable;


/**
 * A COPY fileset implementation that uses temporary files.
 * 
 * @author Brett Henderson
 * 
 */
public class TempCopyFileset implements CopyFileset, Releasable {
	
	private static final Logger LOG = Logger.getLogger(TempCopyFileset.class.getName());
	
	
	private ArrayList<File> tmpFiles;
	private File userFile;
	private File nodeFile;
	private File nodeTagFile;
	private File wayFile;
	private File wayTagFile;
	private File wayNodeFile;
	private File relationFile;
	private File relationTagFile;
	private File relationMemberFile;
	private boolean initialized;
	
	
	/**
	 * Creates a new instance.
	 */
	public TempCopyFileset() {
		tmpFiles = new ArrayList<File>();
		
		initialized = false;
	}
	
	
	private File createTempFile(String suffix) {
		try {
			File tmpFile;
			
			tmpFile = File.createTempFile("copy", suffix);
			tmpFiles.add(tmpFile);
			
			return tmpFile;
			
		} catch (IOException e) {
			throw new OsmosisRuntimeException("Unable to create COPY temp file.", e);
		}
	}
	
	
	private void initialize() {
		if (!initialized) {
			userFile = createTempFile("u");
			nodeFile = createTempFile("n");
			nodeTagFile = createTempFile("nt");
			wayFile = createTempFile("w");
			wayTagFile = createTempFile("wt");
			wayNodeFile = createTempFile("wn");
			relationFile = createTempFile("r");
			relationTagFile = createTempFile("rt");
			relationMemberFile = createTempFile("rm");
			
			initialized = true;
		}
	}
	

	/**
	 * {@inheritDoc}
	 */
	@Override
	public File getNodeFile() {
		initialize();
		
		return nodeFile;
	}

	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public File getNodeTagFile() {
		initialize();
		
		return nodeTagFile;
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public File getRelationFile() {
		initialize();
		
		return relationFile;
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public File getRelationMemberFile() {
		initialize();
		
		return relationMemberFile;
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public File getRelationTagFile() {
		initialize();
		
		return relationTagFile;
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public File getUserFile() {
		initialize();
		
		return userFile;
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public File getWayFile() {
		initialize();
		
		return wayFile;
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public File getWayNodeFile() {
		initialize();
		
		return wayNodeFile;
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public File getWayTagFile() {
		initialize();
		
		return wayTagFile;
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public void release() {
		for (File tmpFile : tmpFiles) {
			if (!tmpFile.delete()) {
				// We cannot throw an exception within a release statement.
				LOG.warning("Unable to delete file " + tmpFile);
			}
		}
		
		tmpFiles.clear();
		initialized = false;
	}
}
