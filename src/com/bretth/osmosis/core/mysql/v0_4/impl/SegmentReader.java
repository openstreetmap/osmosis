package com.bretth.osmosis.core.mysql.v0_4.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

import com.bretth.osmosis.core.OsmosisRuntimeException;
import com.bretth.osmosis.core.database.DatabaseLoginCredentials;
import com.bretth.osmosis.core.domain.v0_4.Segment;
import com.bretth.osmosis.core.mysql.common.BaseEntityReader;
import com.bretth.osmosis.core.mysql.common.DatabaseContext;
import com.bretth.osmosis.core.mysql.common.EntityHistory;


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
	 * @param loginCredentials
	 *            Contains all information required to connect to the database.
	 * @param readAllUsers
	 *            If this flag is true, all users will be read from the database
	 *            regardless of their public edits flag.
	 */
	public SegmentReader(DatabaseLoginCredentials loginCredentials, boolean readAllUsers) {
		super(loginCredentials, readAllUsers);
		
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
