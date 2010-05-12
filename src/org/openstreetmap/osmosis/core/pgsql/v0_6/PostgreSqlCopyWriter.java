// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.pgsql.v0_6;

import org.openstreetmap.osmosis.core.container.v0_6.EntityContainer;
import org.openstreetmap.osmosis.core.pgsql.v0_6.impl.NodeLocationStoreType;
import org.openstreetmap.osmosis.core.pgsql.v0_6.impl.PostgreSqlCopyFilesetBuilder;
import org.openstreetmap.osmosis.core.pgsql.v0_6.impl.TempCopyFileset;


/**
 * An OSM data sink for storing all data to a database using the COPY command.
 * This task is intended for writing to an empty database.
 * 
 * @author Brett Henderson
 */
public class PostgreSqlCopyWriter {
	
	private PostgreSqlCopyFilesetBuilder copyFilesetBuilder;
	private TempCopyFileset copyFileset;
	
	
	/**
	 * Creates a new instance.
	 * 
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
	 */
	public PostgreSqlCopyWriter(
			boolean enableBboxBuilder,
			boolean enableLinestringBuilder, NodeLocationStoreType storeType) {
		copyFileset = new TempCopyFileset();
		
		copyFilesetBuilder =
			new PostgreSqlCopyFilesetBuilder(copyFileset, enableBboxBuilder, enableLinestringBuilder, storeType);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void process(EntityContainer entityContainer) {
		copyFilesetBuilder.process(entityContainer);
	}
	
	
	/**
	 * Writes any buffered data to the files, then loads the files into the database. 
	 */
	public void complete() {
		copyFilesetBuilder.complete();
		
	}
	
	
	/**
	 * Releases all database resources.
	 */
	public void release() {
		copyFilesetBuilder.release();
		copyFileset.release();
	}
}
