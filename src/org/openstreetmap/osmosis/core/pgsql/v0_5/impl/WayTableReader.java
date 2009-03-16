// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package org.openstreetmap.osmosis.core.pgsql.v0_5.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

import org.openstreetmap.osmosis.core.OsmosisRuntimeException;
import org.openstreetmap.osmosis.core.domain.v0_5.OsmUser;
import org.openstreetmap.osmosis.core.domain.v0_5.Way;
import org.openstreetmap.osmosis.core.pgsql.common.BaseTableReader;
import org.openstreetmap.osmosis.core.pgsql.common.DatabaseContext;


/**
 * Reads all ways from a database ordered by their identifier. These ways won't
 * be populated with nodes and tags.
 * 
 * @author Brett Henderson
 */
public class WayTableReader extends BaseTableReader<Way> {
	private String sql;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param dbCtx
	 *            The active connection to use for reading from the database.
	 */
	public WayTableReader(DatabaseContext dbCtx) {
		super(dbCtx);
		
		sql =
			"SELECT w.id, w.user_name, w.tstamp"
			+ " FROM ways w"
			+ " ORDER BY w.id";
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
	public WayTableReader(DatabaseContext dbCtx, String constraintTable) {
		super(dbCtx);
		
		sql =
			"SELECT w.id, w.user_name, w.tstamp"
			+ " FROM ways w"
			+ " INNER JOIN " + constraintTable + " c ON w.id = c.id"
			+ " ORDER BY w.id";
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
	protected ReadResult<Way> createNextValue(ResultSet resultSet) {
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
			throw new OsmosisRuntimeException("Unable to read way fields.", e);
		}
		
		return new ReadResult<Way>(
			true,
			new Way(id, timestamp, user)
		);
	}
}
