package com.bretth.osmosis.core.mysql.impl;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.bretth.osmosis.core.OsmosisRuntimeException;


/**
 * Reads current way segments from a database ordered by the way identifier but not
 * by the sequence.
 * 
 * @author Brett Henderson
 */
public class CurrentWaySegmentTableReader extends BaseTableReader<WaySegment> {
	private static final String SELECT_SQL =
		"SELECT id as way_id, segment_id, sequence_id"
		+ " FROM current_way_segments"
		+ " ORDER BY id";
	
	
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
	public CurrentWaySegmentTableReader(String host, String database, String user, String password) {
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
	protected ReadResult<WaySegment> createNextValue(ResultSet resultSet) {
		long wayId;
		long segmentId;
		int sequenceId;
		
		try {
			wayId = resultSet.getLong("way_id");
			segmentId = resultSet.getLong("segment_id");
			sequenceId = resultSet.getInt("sequence_id");
			
		} catch (SQLException e) {
			throw new OsmosisRuntimeException("Unable to read way segment fields.", e);
		}
		
		return new ReadResult<WaySegment>(
			true,
			new WaySegment(wayId, segmentId, sequenceId)
		);
	}
}
