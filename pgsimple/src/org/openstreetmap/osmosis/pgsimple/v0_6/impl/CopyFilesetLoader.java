// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.pgsimple.v0_6.impl;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.openstreetmap.osmosis.core.OsmosisRuntimeException;
import org.openstreetmap.osmosis.core.database.DatabaseLoginCredentials;
import org.openstreetmap.osmosis.core.database.DatabasePreferences;
import org.openstreetmap.osmosis.pgsimple.common.DatabaseContext;
import org.openstreetmap.osmosis.pgsimple.common.SchemaVersionValidator;
import org.openstreetmap.osmosis.pgsimple.v0_6.PostgreSqlVersionConstants;
import org.postgresql.copy.CopyManager;
import org.postgresql.core.BaseConnection;


/**
 * Loads a COPY fileset into a database.
 * 
 * @author Brett Henderson
 */
public class CopyFilesetLoader implements Runnable {
	
	private static final Logger LOG = Logger.getLogger(CopyFilesetLoader.class.getName());
	
	
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
	 * Loads a table from a COPY file.
	 * 
	 * @param dbCtx
	 *            The database connection.
	 * @param copyFile
	 *            The file to be loaded.
	 * @param tableName
	 *            The table to load the data into.
	 */
    public void loadCopyFile(DatabaseContext dbCtx, File copyFile, String tableName) {
    	CopyManager copyManager;
    	InputStream inStream = null;
    	
    	try {
    		InputStream bufferedInStream;
    		
    		inStream = new FileInputStream(copyFile);
    		bufferedInStream = new BufferedInputStream(inStream, 65536);
    		
    		copyManager = new CopyManager((BaseConnection) dbCtx.getConnection());
    		
    		copyManager.copyIn("COPY " + tableName + " FROM STDIN", bufferedInStream);
			
    		inStream.close();
			inStream = null;
			
    	} catch (IOException e) {
    		throw new OsmosisRuntimeException("Unable to process COPY file " + copyFile + ".", e);
    	} catch (SQLException e) {
    		throw new OsmosisRuntimeException("Unable to process COPY file " + copyFile + ".", e);
    	} finally {
    		if (inStream != null) {
				try {
					inStream.close();
				} catch (Exception e) {
					LOG.log(Level.SEVERE, "Unable to close COPY file.", e);
				}
				inStream = null;
			}
    	}
    }
    

    /**
     * Reads all data from the database and send it to the sink.
     */
    public void run() {
    	DatabaseContext dbCtx = new DatabaseContext(loginCredentials);
    	
    	try {
			IndexManager indexManager;
			
			new SchemaVersionValidator(dbCtx, preferences)
				.validateVersion(PostgreSqlVersionConstants.SCHEMA_VERSION);
    		
    		indexManager = new IndexManager(dbCtx, false, false);
    		
			// Drop all constraints and indexes.
			indexManager.prepareForLoad();
    		
    		LOG.finer("Loading users.");
    		loadCopyFile(dbCtx, copyFileset.getUserFile(), "users");
    		LOG.finer("Loading nodes.");
    		loadCopyFile(dbCtx, copyFileset.getNodeFile(), "nodes");
    		LOG.finer("Loading node tags.");
    		loadCopyFile(dbCtx, copyFileset.getNodeTagFile(), "node_tags");
    		LOG.finer("Loading ways.");
    		loadCopyFile(dbCtx, copyFileset.getWayFile(), "ways");
    		LOG.finer("Loading way tags.");
    		loadCopyFile(dbCtx, copyFileset.getWayTagFile(), "way_tags");
    		LOG.finer("Loading way nodes.");
    		loadCopyFile(dbCtx, copyFileset.getWayNodeFile(), "way_nodes");
    		LOG.finer("Loading relations.");
    		loadCopyFile(dbCtx, copyFileset.getRelationFile(), "relations");
    		LOG.finer("Loading relation tags.");
    		loadCopyFile(dbCtx, copyFileset.getRelationTagFile(), "relation_tags");
    		LOG.finer("Loading relation members.");
    		loadCopyFile(dbCtx, copyFileset.getRelationMemberFile(), "relation_members");
    		LOG.finer("Committing changes.");
    		
    		LOG.fine("Data load complete.");
    		
    		// Add all constraints and indexes.
    		indexManager.completeAfterLoad();
    		
    		LOG.fine("Committing changes.");
    		dbCtx.commit();
    		
    		LOG.fine("Vacuuming database.");
    		dbCtx.setAutoCommit(true);
    		dbCtx.executeStatement("VACUUM ANALYZE");
    		
    		LOG.fine("Complete.");
    		
    	} finally {
    		dbCtx.release();
    	}
    }
}
