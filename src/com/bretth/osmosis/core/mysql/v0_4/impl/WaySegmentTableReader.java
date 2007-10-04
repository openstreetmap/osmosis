package com.bretth.osmosis.core.mysql.v0_4.impl;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.bretth.osmosis.core.OsmosisRuntimeException;
import com.bretth.osmosis.core.database.DatabaseLoginCredentials;
import com.bretth.osmosis.core.mysql.common.BaseTableReader;
import com.bretth.osmosis.core.mysql.common.DatabaseContext;
import com.bretth.osmosis.core.mysql.common.EntityHistory;


/**
 * Reads all way segments from a database ordered by the way identifier but not
 * by the sequence.
 * 
 * @author Brett Henderson
 */
public class WaySegmentTableReader extends BaseTableReader<EntityHistory<WaySegment>> {
	private static final String SELECT_SQL =
		"SELECT id as way_id, version, segment_id, sequence_id"
		+ " FROM way_segments"
		+ " ORDER BY id, version";
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param loginCredentials
	 *            Contains all information required to connect to the database.
	 */
	public WaySegmentTableReader(DatabaseLoginCredentials loginCredentials) {
		super(loginCredentials);
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
			new EntityHistory<WaySegment>(new WaySegment(wayId, segmentId, sequenceId), version, true)
		);
	}
}
