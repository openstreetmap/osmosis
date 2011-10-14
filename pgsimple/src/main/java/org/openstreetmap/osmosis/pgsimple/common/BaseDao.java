// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.pgsimple.common;

import java.sql.PreparedStatement;

import org.openstreetmap.osmosis.core.database.ReleasableStatementContainer;
import org.openstreetmap.osmosis.core.lifecycle.Releasable;


/**
 * Provides functionality common to all dao implementations.
 * 
 * @author Brett Henderson
 */
public class BaseDao implements Releasable {
	
	private DatabaseContext dbCtx;
	private ReleasableStatementContainer statementContainer;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param dbCtx
	 *            The database context to use for accessing the database.
	 */
	protected BaseDao(DatabaseContext dbCtx) {
		this.dbCtx = dbCtx;
		
		statementContainer = new ReleasableStatementContainer();
	}
	
	
	/**
	 * Provides access to the database context. In most cases alternative
	 * methods such as prepareStatement should be used.
	 * 
	 * @return The database context.
	 */
	protected DatabaseContext getDatabaseContext() {
		return dbCtx;
	}
	
	
	/**
	 * Creates a new database prepared statement. This statement will be
	 * automatically released when the dao is released.
	 * 
	 * @param sql
	 *            The statement to be created.
	 * @return The newly created statement.
	 */
	protected PreparedStatement prepareStatement(String sql) {
		return statementContainer.add(dbCtx.prepareStatement(sql));
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void release() {
		statementContainer.release();
	}
}
