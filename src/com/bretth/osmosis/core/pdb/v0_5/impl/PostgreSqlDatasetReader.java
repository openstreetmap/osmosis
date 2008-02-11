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
	private DatabaseLoginCredentials loginCredentials;
	private DatabasePreferences preferences;
	private boolean initialized;
	private DatabaseContext dbCtx;
	private NodeDao nodeDao;
	private WayDao wayDao;
	
	
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
		
		initialized = false;
	}
	
	
	/**
	 * Initialises the database connection and associated data access objects.
	 */
	private void initialize() {
		if (dbCtx == null) {
			dbCtx = new DatabaseContext(loginCredentials);
			
			if (preferences.getValidateSchemaVersion()) {
				new SchemaVersionValidator(loginCredentials).validateVersion(PostgreSqlVersionConstants.SCHEMA_VERSION);
			}
			
			nodeDao = new NodeDao(dbCtx);
			wayDao = new WayDao(dbCtx);
		}
		
		initialized = true;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Node getNode(long id) {
		if (!initialized) {
			initialize();
		}
		
		return nodeDao.getNode(id);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Way getWay(long id) {
		if (!initialized) {
			initialize();
		}
		
		return wayDao.getWay(id);
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
		if (nodeDao != null) {
			nodeDao.release();
		}
		if (dbCtx != null) {
			dbCtx.release();
			
			dbCtx = null;
		}
	}
}
