package com.bretth.osmosis.core.mysql.impl;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.bretth.osmosis.core.OsmosisRuntimeException;


/**
 * Reads all way tags from a database ordered by the way identifier.
 * 
 * @author Brett Henderson
 */
public class WayTagTableReader extends BaseTableReader<EntityHistory<WayTag>> {
	private static final String SELECT_SQL =
		"SELECT id as way_id, version, k, v"
		+ " FROM way_tags"
		+ " ORDER BY way_id, version";
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param host
	 *            The server hosting the database.
	 * @param database
	 *            The database instance.
	 * @param user
	 *            The user name for authentication.
	 * @param password
	 *            The password for authentication.
	 */
	public WayTagTableReader(String host, String database, String user, String password) {
		super(host, database, user, password);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected ResultSet createResultSet(DatabaseContext queryDbCtx) {
		return queryDbCtx.executeStreamingQuery(SELECT_SQL);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected ReadResult<EntityHistory<WayTag>> createNextValue(ResultSet resultSet) {
		long wayId;
		String key;
		String value;
		int version;
		
		try {
			wayId = resultSet.getLong("way_id");
			key = resultSet.getString("k");
			value = resultSet.getString("v");
			version = resultSet.getInt("version");
			
		} catch (SQLException e) {
			throw new OsmosisRuntimeException("Unable to read way tag fields.", e);
		}
		
		return new ReadResult<EntityHistory<WayTag>>(
			true,
			new EntityHistory<WayTag>(new WayTag(wayId, key, value), version, true)
		);
	}
}
