// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package org.openstreetmap.osmosis.core.pgsql.v0_5.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

import org.openstreetmap.osmosis.core.OsmosisRuntimeException;
import org.openstreetmap.osmosis.core.domain.v0_5.OsmUser;
import org.openstreetmap.osmosis.core.domain.v0_5.Relation;
import org.openstreetmap.osmosis.core.pgsql.common.BaseTableReader;
import org.openstreetmap.osmosis.core.pgsql.common.DatabaseContext;


/**
 * Reads all relations from a database ordered by their identifier. These relations won't
 * be populated with members and tags.
 * 
 * @author Brett Henderson
 */
public class RelationTableReader extends BaseTableReader<Relation> {
	private String sql;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param dbCtx
	 *            The active connection to use for reading from the database.
	 */
	public RelationTableReader(DatabaseContext dbCtx) {
		super(dbCtx);
		
		sql =
			"SELECT r.id, r.user_name, r.tstamp"
			+ " FROM relations r"
			+ " ORDER BY r.id";
	}
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param dbCtx
	 *            The active connection to use for reading from the database.
	 * @param constraintTable
	 *            The table containing a column named id defining the list of
	 *            entities to be returned.
	 */
	public RelationTableReader(DatabaseContext dbCtx, String constraintTable) {
		super(dbCtx);
		
		sql = "SELECT r.id, r.user_name, r.tstamp"
			+ " FROM relations r"
			+ " INNER JOIN "
			+ constraintTable
			+ " c ON r.id = c.id"
			+ " ORDER BY r.id";
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected ResultSet createResultSet(DatabaseContext queryDbCtx) {
		return queryDbCtx.executeQuery(sql);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected ReadResult<Relation> createNextValue(ResultSet resultSet) {
		long id;
		Date timestamp;
		OsmUser user;
		
		try {
			id = resultSet.getLong("id");
			if (resultSet.getInt("user_id") != OsmUser.NONE.getId()) {
				user = new OsmUser(resultSet.getInt("user_id"), resultSet.getString("user_name"));
			} else {
				user = OsmUser.NONE;
			}
			timestamp = new Date(resultSet.getTimestamp("tstamp").getTime());
			
		} catch (SQLException e) {
			throw new OsmosisRuntimeException("Unable to read relation fields.", e);
		}
		
		return new ReadResult<Relation>(
			true,
			new Relation(id, timestamp, user)
		);
	}
}
