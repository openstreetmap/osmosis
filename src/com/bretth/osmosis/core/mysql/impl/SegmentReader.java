package com.bretth.osmosis.core.mysql.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

import com.bretth.osmosis.core.OsmosisRuntimeException;
import com.bretth.osmosis.core.domain.v0_4.Segment;


/**
 * Reads all segments from a database ordered by their identifier.
 * 
 * @author Brett Henderson
 */
public class SegmentReader extends BaseEntityReader<EntityHistory<Segment>> {
	private static final String SELECT_SQL =
		"SELECT s.id, s.timestamp, u.data_public, u.display_name, s.node_a, s.node_b, s.tags, s.visible"
		+ " FROM segments s"
		+ " LEFT OUTER JOIN users u ON s.user_id = u.id"
		+ " ORDER BY s.id";
	
	private EmbeddedTagProcessor tagParser;
	
	
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
	 * @param readAllUsers
	 *            If this flag is true, all users will be read from the database
	 *            regardless of their public edits flag.
	 */
	public SegmentReader(String host, String database, String user, String password, boolean readAllUsers) {
		super(host, database, user, password, readAllUsers);
		
		tagParser = new EmbeddedTagProcessor();
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
	protected ReadResult<EntityHistory<Segment>> createNextValue(ResultSet resultSet) {
		long id;
		Date timestamp;
		String userName;
		long from;
		long to;
		String tags;
		boolean visible;
		Segment segment;
		
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
		
		return new ReadResult<EntityHistory<Segment>>(
			true,
			new EntityHistory<Segment>(segment, 0, visible)
		);
	}
}
