// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package com.bretth.osmosis.core.pgsql.v0_6.impl;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.bretth.osmosis.core.OsmosisRuntimeException;
import com.bretth.osmosis.core.domain.v0_6.Tag;
import com.bretth.osmosis.core.mysql.v0_6.impl.DBEntityTag;
import com.bretth.osmosis.core.pgsql.common.DatabaseContext;
import com.bretth.osmosis.core.store.Releasable;


/**
 * Provides operations that are common to all entity daos.
 * 
 * @author Brett Henderson
 */
public abstract class EntityDao implements Releasable {
	
	/**
	 * The database context to use for all database operations.
	 */
	private DatabaseContext dbCtx;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param dbCtx
	 *            The database context to use for accessing the database.
	 */
	protected EntityDao(DatabaseContext dbCtx) {
		this.dbCtx = dbCtx;
	}
	
	
	/**
	 * Provides access to the database context to be used for all database
	 * operations.
	 * 
	 * @return The database context.
	 */
	protected DatabaseContext getDatabaseContext() {
		return dbCtx;
	}
	
	
	/**
	 * Builds a tag from the current result set row.
	 * 
	 * @param resultSet
	 *            The result set.
	 * @return The newly loaded tag.
	 */
	protected DBEntityTag buildTag(ResultSet resultSet) {
		try {
			return new DBEntityTag(
				resultSet.getLong("entity_id"),
				new Tag(
					resultSet.getString("k"),
					resultSet.getString("v")
				)
			);
			
		} catch (SQLException e) {
			throw new OsmosisRuntimeException("Unable to build a tag from the current recordset row.", e);
		} 
	}
}
