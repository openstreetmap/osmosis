package com.bretth.osmosis.core.pdb.v0_5.impl;

import com.bretth.osmosis.core.container.v0_5.DatasetReader;
import com.bretth.osmosis.core.container.v0_5.EntityContainer;
import com.bretth.osmosis.core.database.DatabaseLoginCredentials;
import com.bretth.osmosis.core.database.DatabasePreferences;
import com.bretth.osmosis.core.domain.v0_5.Node;
import com.bretth.osmosis.core.domain.v0_5.Relation;
import com.bretth.osmosis.core.domain.v0_5.Way;
import com.bretth.osmosis.core.pdb.v0_5.PostgreSqlVersionConstants;
import com.bretth.osmosis.core.pgsql.common.DatabaseContext;
import com.bretth.osmosis.core.pgsql.common.SchemaVersionValidator;
import com.bretth.osmosis.core.store.ReleasableIterator;


/**
 * Provides read-only access to a PostgreSQL dataset store. Each thread
 * accessing the store must create its own reader. It is important that all
 * iterators obtained from this reader are released before releasing the reader
 * itself.
 * 
 * @author Brett Henderson
 */
public class PostgreSqlDatasetReader implements DatasetReader {
	private static final String SQL_SELECT_NODE = "SELECT id, tstamp, user_name, coordinate FROM node where id=?";
	
	private DatabaseLoginCredentials loginCredentials;
	private DatabasePreferences preferences;
	private DatabaseContext dbCtx;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param loginCredentials
	 *            Contains all information required to connect to the database.
	 * @param preferences
	 *            Contains preferences configuring database behaviour.
	 */
	public PostgreSqlDatasetReader(DatabaseLoginCredentials loginCredentials, DatabasePreferences preferences) {
		this.loginCredentials = loginCredentials;
		this.preferences = preferences;
	}
	
	
	/**
	 * Returns the database connection and creates one if it doesn't already
	 * exist.
	 * 
	 * @return The database connection.
	 */
	private DatabaseContext getDatabaseContext() {
		if (dbCtx == null) {
			dbCtx = new DatabaseContext(loginCredentials);
			
			if (preferences.getValidateSchemaVersion()) {
				new SchemaVersionValidator(loginCredentials).validateVersion(PostgreSqlVersionConstants.SCHEMA_VERSION);
			}
		}
		
		return dbCtx;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Node getNode(long id) {
		getDatabaseContext().prepareStatement(SQL_SELECT_NODE);
		return null;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Way getWay(long id) {
		//getDatabaseContext().prepareStatement(sql);
		return null;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Relation getRelation(long id) {
		// TODO Auto-generated method stub
		return null;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public ReleasableIterator<EntityContainer> iterate() {
		return null;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public ReleasableIterator<EntityContainer> iterateBoundingBox(double left,
			double right, double top, double bottom, boolean completeWays) {
		return null;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void release() {
		if (dbCtx != null) {
			dbCtx.release();
			
			dbCtx = null;
		}
	}
}
