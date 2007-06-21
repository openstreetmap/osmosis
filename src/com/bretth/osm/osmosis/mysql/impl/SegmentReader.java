package com.bretth.osm.osmosis.mysql.impl;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.bretth.osm.osmosis.OsmosisRuntimeException;
import com.bretth.osm.osmosis.data.Segment;


/**
 * Provides iterator like behaviour for reading segments from a database.
 * 
 * @author Brett Henderson
 */
public class SegmentReader extends EmbeddedTagEntityReader<Segment> {
	private static final String SELECT_SQL =
		"SELECT id, node_a, node_b, tags FROM segments ORDER BY id";
	
	
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
	public SegmentReader(String host, String database, String user, String password) {
		super(host, database, user, password);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Segment createNextValue(ResultSet resultSet) {
		long id;
		long from;
		long to;
		String tags;
		Segment segment;
		
		try {
			id = resultSet.getLong("id");
			from = resultSet.getLong("node_a");
			to = resultSet.getLong("node_b");
			tags = resultSet.getString("tags");
			
		} catch (SQLException e) {
			throw new OsmosisRuntimeException("Unable to read segment fields.", e);
		}
		
		segment = new Segment(id, from, to);
		segment.addTags(parseTags(tags));
		
		return segment;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected String getQuerySql() {
		return SELECT_SQL;
	} 
}
