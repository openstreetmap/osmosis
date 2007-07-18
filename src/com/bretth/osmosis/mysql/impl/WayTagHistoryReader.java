package com.bretth.osmosis.mysql.impl;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;

import com.bretth.osmosis.OsmosisRuntimeException;


/**
 * Reads the set of way segment changes from a database that have occurred within a
 * time interval.
 * 
 * @author Brett Henderson
 */
public class WayTagHistoryReader extends EntityReader<EntityHistory<WayTag>> {
	private static final String SELECT_SQL =
		"SELECT wt.id AS way_id, wt.k, wt.v, wt.version"
		+ " FROM way_tags wt"
		+ " INNER JOIN"
		+ " ("
		+ "SELECT id, MAX(version) AS version"
		+ " FROM ways"
		+ " WHERE timestamp >= ? AND timestamp < ?"
		+ " GROUP BY id"
		+ ") w"
		+ " ON wt.id = w.id AND wt.version = w.version;";
	
	private Date intervalBegin;
	private Date intervalEnd;
	
	
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
	 * @param intervalBegin
	 *            Marks the beginning (inclusive) of the time interval to be
	 *            checked.
	 * @param intervalEnd
	 *            Marks the end (exclusive) of the time interval to be checked.
	 */
	public WayTagHistoryReader(String host, String database, String user, String password, Date intervalBegin, Date intervalEnd) {
		super(host, database, user, password);
		
		this.intervalBegin = intervalBegin;
		this.intervalEnd = intervalEnd;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected ResultSet createResultSet(DatabaseContext queryDbCtx) {
		try {
			PreparedStatement statement;
			
			statement = queryDbCtx.prepareStatementForStreaming(SELECT_SQL);
			statement.setTimestamp(1, new Timestamp(intervalBegin.getTime()));
			statement.setTimestamp(2, new Timestamp(intervalEnd.getTime()));
			
			return statement.executeQuery();
			
		} catch (SQLException e) {
			throw new OsmosisRuntimeException("Unable to create streaming resultset.", e);
		}
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected EntityHistory<WayTag> createNextValue(ResultSet resultSet) {
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
			throw new OsmosisRuntimeException("Unable to read way segment fields.", e);
		}
		
		return new EntityHistory<WayTag>(new WayTag(wayId, key, value), version, true);
	}
}
