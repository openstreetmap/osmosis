// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.pgsnapshot.v0_6.impl;

import java.io.File;


/**
 * A COPY fileset implementation that defines fixed filenames within a specified
 * directory.
 * 
 * @author Brett Henderson
 * 
 */
public class DirectoryCopyFileset implements CopyFileset {
	private static final String USER_SUFFIX = "users.txt";
	private static final String NODE_SUFFIX = "nodes.txt";
	private static final String WAY_SUFFIX = "ways.txt";
	private static final String WAY_NODE_SUFFIX = "way_nodes.txt";
	private static final String RELATION_SUFFIX = "relations.txt";
	private static final String RELATION_MEMBER_SUFFIX = "relation_members.txt";
	
	
	private File directory;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param directory
	 *            The directory to store all files in.
	 */
	public DirectoryCopyFileset(File directory) {
		this.directory = directory;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public File getNodeFile() {
		return new File(directory, NODE_SUFFIX);
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public File getRelationFile() {
		return new File(directory, RELATION_SUFFIX);
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public File getRelationMemberFile() {
		return new File(directory, RELATION_MEMBER_SUFFIX);
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public File getUserFile() {
		return new File(directory, USER_SUFFIX);
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public File getWayFile() {
		return new File(directory, WAY_SUFFIX);
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public File getWayNodeFile() {
		return new File(directory, WAY_NODE_SUFFIX);
	}
}
