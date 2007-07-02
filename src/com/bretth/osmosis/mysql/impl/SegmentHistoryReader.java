package com.bretth.osmosis.mysql.impl;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;

import com.bretth.osmosis.OsmosisRuntimeException;
import com.bretth.osmosis.data.Segment;


/**
 * Reads all segment history items for a single segment from a database ordered by
 * their identifier.
 * 
 * @author Brett Henderson
 */
public class SegmentHistoryReader extends EntityReader<SegmentHistory> {
	private static final String SELECT_SQL =
		"SELECT id, node_a, node_b, tags, visible FROM segments WHERE id = ? AND timestamp < ? ORDER BY timestamp";
	
	private EmbeddedTagParser tagParser;
	private Long segmentId;
	private Date intervalEnd;
	private PreparedStatement statement;
	
	
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
	public SegmentHistoryReader(String host, String database, String user, String password) {
		super(host, database, user, password);
		
		tagParser = new EmbeddedTagParser();
	}
	
	
	/**
	 * Specifies the id of the segment to query history for.
	 * 
	 * @param segmentId
	 *            The segment identifier.
	 */
	public void setSegmentId(long segmentId) {
		this.segmentId = segmentId;
	}
	
	
	/**
	 * Specifies the end of the period for which we should search history for.
	 * 
	 * @param intervalEnd
	 *            The end of the time interval.
	 */
	public void setIntervalEnd(Date intervalEnd) {
		this.intervalEnd = intervalEnd;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected ResultSet createResultSet(DatabaseContext queryDbCtx) {
		try {
			if (statement == null) {
				statement = queryDbCtx.prepareStatementForStreaming(SELECT_SQL);
			}
			
			statement.setLong(1, segmentId);
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
	protected SegmentHistory createNextValue(ResultSet resultSet) {
		long id;
		long from;
		long to;
		String tags;
		boolean visible;
		Segment segment;
		SegmentHistory segmentHistory;
		
		try {
			id = resultSet.getLong("id");
			from = resultSet.getLong("node_a");
			to = resultSet.getLong("node_b");
			tags = resultSet.getString("tags");
			visible = resultSet.getBoolean("visible");
			
		} catch (SQLException e) {
			throw new OsmosisRuntimeException("Unable to read segment fields.", e);
		}
		
		segment = new Segment(id, from, to);
		segment.addTags(tagParser.parseTags(tags));
		
		segmentHistory = new SegmentHistory(segment, visible);
		
		return segmentHistory;
	}
}
