// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.pgsql.v0_6;

import org.openstreetmap.osmosis.core.container.v0_6.EntityContainer;
import org.openstreetmap.osmosis.core.database.DatabaseLoginCredentials;
import org.openstreetmap.osmosis.core.database.DatabasePreferences;
import org.openstreetmap.osmosis.core.pgsql.common.DatabaseContext;
import org.openstreetmap.osmosis.core.pgsql.common.NodeLocationStoreType;
import org.openstreetmap.osmosis.core.pgsql.v0_6.impl.CopyFilesetLoader;
import org.openstreetmap.osmosis.core.pgsql.v0_6.impl.DatabaseCapabilityChecker;
import org.openstreetmap.osmosis.core.pgsql.v0_6.impl.CopyFilesetBuilder;
import org.openstreetmap.osmosis.core.pgsql.v0_6.impl.TempCopyFileset;
import org.openstreetmap.osmosis.core.task.v0_6.Sink;


/**
 * An OSM data sink for storing all data to a database using the COPY command.
 * This task is intended for writing to an empty database.
 * 
 * @author Brett Henderson
 */
public class PostgreSqlCopyWriter implements Sink {
	
	private CopyFilesetBuilder copyFilesetBuilder;
	private CopyFilesetLoader copyFilesetLoader;
	private TempCopyFileset copyFileset;
	private DatabaseLoginCredentials loginCredentials;
	private DatabasePreferences preferences;
	private NodeLocationStoreType storeType;
	private boolean populateBbox;
	private boolean populateLinestring;
	private boolean initialized;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param loginCredentials
	 *            Contains all information required to connect to the database.
	 * @param preferences
	 *            Contains preferences configuring database behaviour.
	 * @param storeType
	 *            The node location storage type used by the geometry builders.
	 */
	public PostgreSqlCopyWriter(
			DatabaseLoginCredentials loginCredentials, DatabasePreferences preferences,
			NodeLocationStoreType storeType) {
		this.loginCredentials = loginCredentials;
		this.preferences = preferences;
		this.storeType = storeType;
		
		copyFileset = new TempCopyFileset();
	}
	
	
	private void initialize() {
		if (!initialized) {
			DatabaseContext dbCtx;
			DatabaseCapabilityChecker capabilityChecker;
			
			dbCtx = new DatabaseContext(loginCredentials);
			
			try {
				capabilityChecker = new DatabaseCapabilityChecker(dbCtx);

				populateBbox = capabilityChecker.isWayBboxSupported();
				populateLinestring = capabilityChecker.isWayLinestringSupported();				

				copyFilesetBuilder =
					new CopyFilesetBuilder(copyFileset, populateBbox, populateLinestring, storeType);
				
				copyFilesetLoader = new CopyFilesetLoader(loginCredentials, preferences, copyFileset);
				
			} finally {
				dbCtx.release();
			}
			
			initialized = true;
		}
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void process(EntityContainer entityContainer) {
		initialize();
		
		copyFilesetBuilder.process(entityContainer);
	}
	
	
	/**
	 * Writes any buffered data to the files, then loads the files into the database. 
	 */
	public void complete() {
		initialize();
		
		copyFilesetBuilder.complete();
		copyFilesetLoader.run();
	}
	
	
	/**
	 * Releases all database resources.
	 */
	public void release() {
		copyFilesetBuilder.release();
		copyFileset.release();
		
		initialized = false;
	}
}
