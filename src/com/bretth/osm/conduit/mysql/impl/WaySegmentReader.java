package com.bretth.osm.conduit.mysql.impl;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.bretth.osm.conduit.ConduitRuntimeException;


public class WaySegmentReader extends EntityReader<WaySegment> {
	private static final String SELECT_SQL =
		"SELECT id AS way_id, segment_id, sequence_id FROM way_segments ORDER BY way_id, sequence_id";
	
	
	protected WaySegment createNextValue(ResultSet resultSet) {
		long wayId;
		long segmentId;
		int sequenceId;
		
		try {
			wayId = resultSet.getLong("way_id");
			segmentId = resultSet.getLong("segment_id");
			sequenceId = resultSet.getInt("sequence_id");
			
		} catch (SQLException e) {
			throw new ConduitRuntimeException("Unable to read way segment fields.", e);
		}
		
		return new WaySegment(wayId, segmentId, sequenceId);
	}
	
	
	protected String getQuerySql() {
		return SELECT_SQL;
	} 
}
