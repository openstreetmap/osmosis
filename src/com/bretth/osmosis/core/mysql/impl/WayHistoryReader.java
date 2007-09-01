package com.bretth.osmosis.core.mysql.impl;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;

import com.bretth.osmosis.core.OsmosisRuntimeException;
import com.bretth.osmosis.core.domain.Way;


/**
 * Reads the set of way changes from a database that have occurred within a
 * time interval.
 * 
 * @author Brett Henderson
 */
public class WayHistoryReader extends BaseEntityReader<EntityHistory<Way>> {
	private static final String SELECT_SQL =
		"SELECT w.id AS id, w.timestamp AS timestamp, w.version AS version, w.visible AS visible" +
		" FROM ways w" +
		" INNER JOIN users u ON w.user_id = u.id" +
		" WHERE w.timestamp > ? AND w.timestamp <= ?" +
		" ORDER BY w.id, w.version";
	
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
	protected ReadResult<EntityHistory<Way>> createNextValue(ResultSet resultSet) {
		long id;
		Date timestamp;
		boolean dataPublic;
		String userName;
		int version;
		boolean visible;
		
		try {
			id = resultSet.getLong("id");
			timestamp = new Date(resultSet.getTimestamp("timestamp").getTime());
			dataPublic = resultSet.getBoolean("data_public");
			userName = resultSet.getString("display_name");
			version = resultSet.getInt("version");
			visible = resultSet.getBoolean("visible");
			
		} catch (SQLException e) {
			throw new OsmosisRuntimeException("Unable to read node fields.", e);
		}
		
		if (!dataPublic) {
			userName = "";
		}
		
		return new ReadResult<EntityHistory<Way>>(
			true,
			new EntityHistory<Way>(
				new Way(id, timestamp, userName), version, visible)
		);
	}
}
