package com.bretth.osmosis.core.mysql.impl;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;

import com.bretth.osmosis.core.OsmosisRuntimeException;


/**
 * Reads the most recent set of way tags from a database for ways that have been
 * modified within a time interval.
 * 
 * @author Brett Henderson
 */
public class WayTagHistoryReader extends BaseTableReader<EntityHistory<WayTag>> {
	private static final String SELECT_SQL =
		"SELECT wt.id AS way_id, wt.k, wt.v, wt.version" +
		" FROM way_tags wt" +
		" INNER JOIN (" +
		"   SELECT id, MAX(version) as version" +
		"   FROM ways" +
		"   WHERE timestamp > ? AND timestamp <= ?" +
		"   GROUP BY id" +
		" ) wayList ON wt.id = wayList.id AND wt.version = wayList.version";
	
	private Date intervalBegin;
	private Date intervalEnd;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param loginCredentials
	 *            Contains all information required to connect to the database.
	 * @param intervalBegin
	 *            Marks the beginning (inclusive) of the time interval to be
	 *            checked.
	 * @param intervalEnd
	 *            Marks the end (exclusive) of the time interval to be checked.
	 */
	public WayTagHistoryReader(DatabaseLoginCredentials loginCredentials, Date intervalBegin, Date intervalEnd) {
		super(loginCredentials);
		
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
			throw new OsmosisRuntimeException("Unable to read way segment fields.", e);
		}
		
		return new ReadResult<EntityHistory<WayTag>>(
			true,
			new EntityHistory<WayTag>(
				new WayTag(wayId, key, value), version, true)
		);
	}
}
