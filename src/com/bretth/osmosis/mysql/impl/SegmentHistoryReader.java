package com.bretth.osmosis.mysql.impl;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;

import com.bretth.osmosis.OsmosisRuntimeException;
import com.bretth.osmosis.data.Segment;


/**
 * Reads segment history records for segments that have been modified within a time
 * interval. All history items will be returned for the segment from segment creation
 * up to the end of the time interval.
 * 
 * @author Brett Henderson
 */
public class SegmentHistoryReader extends EntityReader<EntityHistory<Segment>> {
	// The sub-select identifies the segments that have been modified within the
	// time interval. The outer query then queries all segment history items up to
	// the end of the time interval.
	private static final String SELECT_SQL =
		"SELECT id, timestamp, node_a, node_b, tags, visible"
		+ " FROM segments"
		+ " WHERE id IN ("
		+ "SELECT id FROM segments WHERE timestamp >= ? AND timestamp < ?"
		+ ") AND timestamp < ?"
		+ " ORDER BY id, timestamp";
	
	private EmbeddedTagProcessor tagParser;
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
	public SegmentHistoryReader(String host, String database, String user, String password, Date intervalBegin, Date intervalEnd) {
		super(host, database, user, password);
		
		this.intervalBegin = intervalBegin;
		this.intervalEnd = intervalEnd;
		
		tagParser = new EmbeddedTagProcessor();
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
			statement.setTimestamp(3, new Timestamp(intervalEnd.getTime()));
			
			return statement.executeQuery();
			
		} catch (SQLException e) {
			throw new OsmosisRuntimeException("Unable to create streaming resultset.", e);
		}
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected EntityHistory<Segment> createNextValue(ResultSet resultSet) {
		long id;
		Date timestamp;
		long from;
		long to;
		String tags;
		boolean visible;
		Segment segment;
		EntityHistory<Segment> segmentHistory;
		
		try {
			id = resultSet.getLong("id");
			timestamp = resultSet.getTimestamp("timestamp");
			from = resultSet.getLong("node_a");
			to = resultSet.getLong("node_b");
			tags = resultSet.getString("tags");
			visible = resultSet.getBoolean("visible");
			
		} catch (SQLException e) {
			throw new OsmosisRuntimeException("Unable to read segment fields.", e);
		}
		
		segment = new Segment(id, timestamp, from, to);
		segment.addTags(tagParser.parseTags(tags));
		
		segmentHistory = new EntityHistory<Segment>(segment, 0, visible);
		
		return segmentHistory;
	}
}
