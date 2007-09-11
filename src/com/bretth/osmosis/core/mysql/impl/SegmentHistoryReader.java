package com.bretth.osmosis.core.mysql.impl;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;

import com.bretth.osmosis.core.OsmosisRuntimeException;
import com.bretth.osmosis.core.domain.v0_4.Segment;


/**
 * Reads segment history records for segments that have been modified within a time
 * interval. All history items will be returned for the segment from segment creation
 * up to the end of the time interval. We need the complete history instead of
 * just the history within the interval so we can determine if the segment was
 * created during the interval or prior to the interval, a version attribute
 * would eliminate the need for full history.
 * 
 * @author Brett Henderson
 */
public class SegmentHistoryReader extends BaseEntityReader<EntityHistory<Segment>> {
	// The sub-select identifies the segments that have been modified within the
	// time interval. The outer query then queries all segment history items up to
	// the end of the time interval.
	private static final String SELECT_SQL =
		"SELECT s.id, s.timestamp, u.data_public, u.display_name, s.node_a, s.node_b, s.tags, s.visible" +
		" FROM segments s" +
		" INNER JOIN (" +
		"   SELECT id" +
		"   FROM segments" +
		"   WHERE timestamp > ? AND timestamp <= ?" +
		"   GROUP BY id" +
		" ) idList ON s.id = idList.id" +
		" LEFT OUTER JOIN users u ON s.user_id = u.id" +
		" WHERE s.timestamp < ?" +
		" ORDER BY s.id, s.timestamp";
	
	private EmbeddedTagProcessor tagParser;
	private Date intervalBegin;
	private Date intervalEnd;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param loginCredentials
	 *            Contains all information required to connect to the database.
	 * @param readAllUsers
	 *            If this flag is true, all users will be read from the database
	 *            regardless of their public edits flag.
	 * @param intervalBegin
	 *            Marks the beginning (inclusive) of the time interval to be
	 *            checked.
	 * @param intervalEnd
	 *            Marks the end (exclusive) of the time interval to be checked.
	 */
	public SegmentHistoryReader(DatabaseLoginCredentials loginCredentials, boolean readAllUsers, Date intervalBegin, Date intervalEnd) {
		super(loginCredentials, readAllUsers);
		
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
	protected ReadResult<EntityHistory<Segment>> createNextValue(ResultSet resultSet) {
		long id;
		Date timestamp;
		String userName;
		long from;
		long to;
		String tags;
		boolean visible;
		Segment segment;
		EntityHistory<Segment> segmentHistory;
		
		try {
			id = resultSet.getLong("id");
			timestamp = new Date(resultSet.getTimestamp("timestamp").getTime());
			userName = readUserField(
				resultSet.getBoolean("data_public"),
				resultSet.getString("display_name")
			);
			from = resultSet.getLong("node_a");
			to = resultSet.getLong("node_b");
			tags = resultSet.getString("tags");
			visible = resultSet.getBoolean("visible");
			
		} catch (SQLException e) {
			throw new OsmosisRuntimeException("Unable to read segment fields.", e);
		}
		
		segment = new Segment(id, timestamp, userName, from, to);
		segment.addTags(tagParser.parseTags(tags));
		
		segmentHistory = new EntityHistory<Segment>(segment, 0, visible);
		
		return new ReadResult<EntityHistory<Segment>>(true, segmentHistory);
	}
}
