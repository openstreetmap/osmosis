// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.pgsnapshot.v0_6.impl;

import java.util.logging.Logger;

import org.openstreetmap.osmosis.core.database.DatabaseLoginCredentials;
import org.openstreetmap.osmosis.core.database.DatabasePreferences;
import org.openstreetmap.osmosis.pgsnapshot.common.DatabaseContext;
import org.openstreetmap.osmosis.pgsnapshot.common.SchemaVersionValidator;
import org.openstreetmap.osmosis.pgsnapshot.v0_6.PostgreSqlVersionConstants;


/**
 * Loads a COPY fileset into a database.
 * 
 * @author Brett Henderson
 */
public class CopyFilesetLoader implements Runnable {
	
	private static final Logger LOG = Logger.getLogger(CopyFilesetLoader.class.getName());
	
	
	private static String[] appendColumn(String[] columns, String newColumn) {
		String[] result;
		
		result = new String[columns.length + 1];
		
		System.arraycopy(columns, 0, result, 0, columns.length);
		result[columns.length] = newColumn;
		
		return result;
	}
	
	
	private static final String[] COMMON_COLUMNS = {"id", "version", "user_id", "tstamp", "changeset_id", "tags"};
	private static final String[] NODE_COLUMNS = appendColumn(COMMON_COLUMNS, "geom");
	private static final String[] WAY_COLUMNS = appendColumn(COMMON_COLUMNS, "nodes");
	private static final String[] RELATION_COLUMNS = COMMON_COLUMNS;
	
	
	private DatabaseLoginCredentials loginCredentials;
	private DatabasePreferences preferences;
	private CopyFileset copyFileset;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param loginCredentials
	 *            Contains all information required to connect to the database.
	 * @param preferences
	 *            Contains preferences configuring database behaviour.
	 * @param copyFileset
	 *            The set of COPY files to be loaded into the database.
	 */
	public CopyFilesetLoader(DatabaseLoginCredentials loginCredentials, DatabasePreferences preferences,
			CopyFileset copyFileset) {
		this.loginCredentials = loginCredentials;
		this.preferences = preferences;
		this.copyFileset = copyFileset;
	}
    

    /**
     * Reads all data from the database and send it to the sink.
     */
    public void run() {
    	DatabaseContext dbCtx = new DatabaseContext(loginCredentials);
    	
    	try {
    		DatabaseCapabilityChecker capabilityChecker;
			IndexManager indexManager;
			String[] wayColumns;
			
			dbCtx.beginTransaction();
			
			capabilityChecker = new DatabaseCapabilityChecker(dbCtx);
			new SchemaVersionValidator(dbCtx.getJdbcTemplate(), preferences)
				.validateVersion(PostgreSqlVersionConstants.SCHEMA_VERSION);
			
			wayColumns = WAY_COLUMNS;
			if (capabilityChecker.isWayBboxSupported()) {
				wayColumns = appendColumn(wayColumns, "bbox");
			}
			if (capabilityChecker.isWayLinestringSupported()) {
				wayColumns = appendColumn(wayColumns, "linestring");
			}
    		
    		indexManager = new IndexManager(dbCtx, false, false);
    		
			// Drop all constraints and indexes.
			indexManager.prepareForLoad();
    		
    		LOG.finer("Loading users.");
    		dbCtx.loadCopyFile(copyFileset.getUserFile(), "users");
    		LOG.finer("Loading nodes.");
    		dbCtx.loadCopyFile(copyFileset.getNodeFile(), "nodes", NODE_COLUMNS);
    		LOG.finer("Loading ways.");
    		dbCtx.loadCopyFile(copyFileset.getWayFile(), "ways", wayColumns);
    		LOG.finer("Loading way nodes.");
    		dbCtx.loadCopyFile(copyFileset.getWayNodeFile(), "way_nodes");
    		LOG.finer("Loading relations.");
    		dbCtx.loadCopyFile(copyFileset.getRelationFile(), "relations", RELATION_COLUMNS);
    		LOG.finer("Loading relation members.");
    		dbCtx.loadCopyFile(copyFileset.getRelationMemberFile(), "relation_members");
    		LOG.finer("Committing changes.");
    		
    		LOG.fine("Data load complete.");
    		
    		// Add all constraints and indexes.
    		indexManager.completeAfterLoad();
    		
    		dbCtx.commitTransaction();
    		
    		LOG.fine("Clustering database.");
    		dbCtx.getJdbcTemplate().update("CLUSTER");
    		
    		LOG.fine("Vacuuming database.");
    		dbCtx.getJdbcTemplate().update("VACUUM ANALYZE");
    		
    		LOG.fine("Complete.");
    		
    	} finally {
    		dbCtx.release();
    	}
    }
}
