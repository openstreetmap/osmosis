package com.bretth.osmosis.core.mysql.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

import com.bretth.osmosis.core.OsmosisRuntimeException;
import com.bretth.osmosis.core.domain.v0_4.Way;


/**
 * Reads current ways from a database ordered by their identifier. These ways
 * won't be populated with segments and tags.
 * 
 * @author Brett Henderson
 */
public class CurrentWayTableReader extends BaseEntityReader<Way> {
	private static final String SELECT_SQL =
		"SELECT w.id, w.timestamp, u.data_public, u.display_name, w.visible"
		+ " FROM current_ways w"
		+ " INNER JOIN users u ON w.user_id = u.id"
		+ " ORDER BY w.id, w.version";
	
	
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
	 * @param readAllUsers
	 *            If this flag is true, all users will be read from the database
	 *            regardless of their public edits flag.
	 */
	public CurrentWayTableReader(String host, String database, String user, String password, boolean readAllUsers) {
		super(host, database, user, password, readAllUsers);
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
	protected ReadResult<Way> createNextValue(ResultSet resultSet) {
		long id;
		Date timestamp;
		String userName;
		boolean visible;
		
		try {
			id = resultSet.getLong("id");
			timestamp = new Date(resultSet.getTimestamp("timestamp").getTime());
			userName = readUserField(
				resultSet.getBoolean("data_public"),
				resultSet.getString("display_name")
			);
			visible = resultSet.getBoolean("visible");
			
		} catch (SQLException e) {
			throw new OsmosisRuntimeException("Unable to read way fields.", e);
		}
		
		// Non-visible records will be ignored by the caller.
		return new ReadResult<Way>(
			visible,
			new Way(id, timestamp, userName)
		);
	}
}
