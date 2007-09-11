package com.bretth.osmosis.core.mysql.impl;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;

import com.bretth.osmosis.core.OsmosisRuntimeException;


/**
 * Reads the most recent set of way segments from a database for ways that have
 * been modified within a time interval.
 * 
 * @author Brett Henderson
 */
public class WaySegmentHistoryReader extends BaseTableReader<EntityHistory<WaySegment>> {
	private static final String SELECT_SQL =
		"SELECT ws.id AS way_id, ws.segment_id, ws.sequence_id, ws.version" +
		" FROM way_segments ws" +
		" INNER JOIN (" +
		"   SELECT id, MAX(version) as version" +
		"   FROM ways" +
		"   WHERE timestamp > ? AND timestamp <= ?" +
		"   GROUP BY id" +
		" ) wayList ON ws.id = wayList.id AND ws.version = wayList.version";
	
	
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
	public WaySegmentHistoryReader(DatabaseLoginCredentials loginCredentials, Date intervalBegin, Date intervalEnd) {
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
	protected ReadResult<EntityHistory<WaySegment>> createNextValue(ResultSet resultSet) {
		long wayId;
		long segmentId;
		int sequenceId;
		int version;
		
		try {
			wayId = resultSet.getLong("way_id");
			segmentId = resultSet.getLong("segment_id");
			sequenceId = resultSet.getInt("sequence_id");
			version = resultSet.getInt("version");
			
		} catch (SQLException e) {
			throw new OsmosisRuntimeException("Unable to read way segment fields.", e);
		}
		
		return new ReadResult<EntityHistory<WaySegment>>(
			true,
			new EntityHistory<WaySegment>(
					new WaySegment(wayId, segmentId, sequenceId), version, true)
		);
	}
}
