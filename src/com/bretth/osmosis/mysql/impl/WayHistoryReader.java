package com.bretth.osmosis.mysql.impl;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;

import com.bretth.osmosis.OsmosisRuntimeException;
import com.bretth.osmosis.data.Way;


/**
 * Reads the set of way changes from a database that have occurred within a
 * time interval.
 * 
 * @author Brett Henderson
 */
public class WayHistoryReader extends EntityReader<EntityHistory<Way>> {
	private static final String SELECT_SQL =
		"SELECT w.id AS id, w.timestamp AS timestamp, w.version AS version, w.visible AS visible"
		+ " FROM ways w"
		+ " INNER JOIN"
		+ " ("
		+ "SELECT id, MAX(version) AS version"
		+ " FROM ways"
		+ " WHERE timestamp >= ? AND timestamp < ?"
		+ " GROUP BY id"
		+ ") w2"
		+ " ON w.id = w2.id AND w.version = w2.version;";
	
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
	public WayHistoryReader(String host, String database, String user, String password, Date intervalBegin, Date intervalEnd) {
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
	protected EntityHistory<Way> createNextValue(ResultSet resultSet) {
		long id;
		Date timestamp;
		int version;
		boolean visible;
		
		try {
			id = resultSet.getLong("id");
			timestamp = resultSet.getTimestamp("timestamp");
			version = resultSet.getInt("version");
			visible = resultSet.getBoolean("visible");
			
		} catch (SQLException e) {
			throw new OsmosisRuntimeException("Unable to read node fields.", e);
		}
		
		return new EntityHistory<Way>(new Way(id, timestamp), version, visible);
	}
}
