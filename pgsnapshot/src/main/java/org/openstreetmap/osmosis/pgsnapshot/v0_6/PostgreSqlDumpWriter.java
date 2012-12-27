// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.pgsnapshot.v0_6;

import java.io.File;
import java.util.Map;

import org.openstreetmap.osmosis.core.container.v0_6.EntityContainer;
import org.openstreetmap.osmosis.pgsnapshot.common.NodeLocationStoreType;
import org.openstreetmap.osmosis.pgsnapshot.v0_6.impl.DirectoryCopyFileset;
import org.openstreetmap.osmosis.pgsnapshot.v0_6.impl.CopyFilesetBuilder;
import org.openstreetmap.osmosis.core.task.v0_6.Sink;


/**
 * An OSM data sink for storing all data to database dump files. This task is
 * intended for populating an empty database.
 * 
 * @author Brett Henderson
 */
public class PostgreSqlDumpWriter implements Sink {
	
	private CopyFilesetBuilder copyFilesetBuilder;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param filePrefix
	 *            The prefix to prepend to all generated file names.
	 * @param enableBboxBuilder
	 *            If true, the way bbox geometry is built during processing
	 *            instead of relying on the database to build them after import.
	 *            This increases processing but is faster than relying on the
	 *            database.
	 * @param enableLinestringBuilder
	 *            If true, the way linestring geometry is built during
	 *            processing instead of relying on the database to build them
	 *            after import. This increases processing but is faster than
	 *            relying on the database.
	 * @param storeType
	 *            The node location storage type used by the geometry builders.
	 * @param keepInvalidWays
	 *            If true, zero and single node ways are kept. Otherwise they are
	 *            silently dropped to avoid putting invalid geometries into the 
	 *            database which can cause problems with postgis functions.
	 */
	public PostgreSqlDumpWriter(
			File filePrefix, boolean enableBboxBuilder,
			boolean enableLinestringBuilder, NodeLocationStoreType storeType, 
			boolean keepInvalidWays) {
		DirectoryCopyFileset copyFileset;
		
		copyFileset = new DirectoryCopyFileset(filePrefix);
		
		copyFilesetBuilder =
			new CopyFilesetBuilder(copyFileset, enableBboxBuilder, enableLinestringBuilder, storeType, keepInvalidWays);
	}
    
    
    /**
     * {@inheritDoc}
     */
    public void initialize(Map<String, Object> metaData) {
		// Do nothing.
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void process(EntityContainer entityContainer) {
		copyFilesetBuilder.process(entityContainer);
	}
	
	
	/**
	 * Writes any buffered data to the database and commits. 
	 */
	public void complete() {
		copyFilesetBuilder.complete();
	}
	
	
	/**
	 * Releases all database resources.
	 */
	public void release() {
		copyFilesetBuilder.release();
	}
}
