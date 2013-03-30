// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.pgsimple.v0_6;

import java.util.Map;
import java.util.logging.Logger;

import org.openstreetmap.osmosis.core.container.v0_6.EntityContainer;
import org.openstreetmap.osmosis.core.database.DatabaseLoginCredentials;
import org.openstreetmap.osmosis.core.database.DatabasePreferences;
import org.openstreetmap.osmosis.pgsimple.common.DatabaseContext;
import org.openstreetmap.osmosis.pgsimple.common.NodeLocationStoreType;
import org.openstreetmap.osmosis.pgsimple.v0_6.impl.CopyFilesetLoader;
import org.openstreetmap.osmosis.pgsimple.v0_6.impl.DatabaseCapabilityChecker;
import org.openstreetmap.osmosis.pgsimple.v0_6.impl.CopyFilesetBuilder;
import org.openstreetmap.osmosis.pgsimple.v0_6.impl.TempCopyFileset;
import org.openstreetmap.osmosis.core.task.v0_6.Sink;


/**
 * An OSM data sink for storing all data to a database using the COPY command.
 * This task is intended for writing to an empty database.
 * 
 * @author Brett Henderson
 */
public class PostgreSqlCopyWriter implements Sink {
	
	private static final Logger LOG = Logger.getLogger(PostgreSqlCopyWriter.class.getName());
	
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
			
			LOG.fine("Initializing the database and temporary processing files.");
			
			dbCtx = new DatabaseContext(loginCredentials);
			
			try {
				capabilityChecker = new DatabaseCapabilityChecker(dbCtx);

				populateBbox = capabilityChecker.isWayBboxSupported();
				populateLinestring = capabilityChecker.isWayLinestringSupported();				

				copyFilesetBuilder =
					new CopyFilesetBuilder(copyFileset, populateBbox, populateLinestring, storeType);
				
				copyFilesetLoader = new CopyFilesetLoader(loginCredentials, preferences, copyFileset);
				
				LOG.fine("Processing input data, building geometries and creating database load files.");
				
			} finally {
				dbCtx.release();
			}
			
			initialized = true;
		}
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
		initialize();
		
		copyFilesetBuilder.process(entityContainer);
	}
	
	
	/**
	 * Writes any buffered data to the files, then loads the files into the database. 
	 */
	public void complete() {
		initialize();
		
		LOG.fine("All data has been received, beginning database load.");
		copyFilesetBuilder.complete();
		copyFilesetLoader.run();
		
		LOG.fine("Processing complete.");
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
