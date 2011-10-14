// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.pgsnapshot.v0_6.impl;

import java.io.File;


/**
 * A copy fileset is a collection of files in the PostgreSQL "COPY" format that
 * can be used to populate the database.
 * 
 * @author Brett Henderson
 */
public interface CopyFileset {
	/**
	 * Gets the user table file.
	 * 
	 * @return The user table file.
	 */
	File getUserFile();


	/**
	 * Gets the node table file.
	 * 
	 * @return The node table file.
	 */
	File getNodeFile();


	/**
	 * Gets the way table file.
	 * 
	 * @return The way table file.
	 */
	File getWayFile();


	/**
	 * Gets the way node table file.
	 * 
	 * @return The way node table file.
	 */
	File getWayNodeFile();


	/**
	 * Gets the relation table file.
	 * 
	 * @return The relation table file.
	 */
	File getRelationFile();


	/**
	 * Gets the relation member table file.
	 * 
	 * @return The relation member table file.
	 */
	File getRelationMemberFile();
}
