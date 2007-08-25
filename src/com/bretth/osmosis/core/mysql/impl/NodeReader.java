package com.bretth.osmosis.core.mysql.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

import com.bretth.osmosis.core.OsmosisRuntimeException;
import com.bretth.osmosis.core.domain.Node;


/**
 * Reads complete node history from a database ordered by their identifier.
 * 
 * @author Brett Henderson
 */
public class NodeReader extends BaseEntityReader<EntityHistory<Node>> {
	private static final String SELECT_SQL =
		"SELECT id, timestamp, latitude, longitude, tags, visible"
		+ " FROM nodes"
		+ " ORDER BY id";
	
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
	 */
	public NodeReader(String host, String database, String user, String password) {
		super(host, database, user, password);
		
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
	protected ReadResult<EntityHistory<Node>> createNextValue(ResultSet resultSet) {
		long id;
		Date timestamp;
		double latitude;
		double longitude;
		String tags;
		boolean visible;
		Node node;
		
		try {
			id = resultSet.getLong("id");
			timestamp = new Date(resultSet.getTimestamp("timestamp").getTime());
			latitude = resultSet.getDouble("latitude");
			longitude = resultSet.getDouble("longitude");
			tags = resultSet.getString("tags");
			visible = resultSet.getBoolean("visible");
			
		} catch (SQLException e) {
			throw new OsmosisRuntimeException("Unable to read node fields.", e);
		}
		
		node = new Node(id, timestamp, latitude, longitude);
		node.addTags(tagParser.parseTags(tags));
		
		return new ReadResult<EntityHistory<Node>>(
			true,
			new EntityHistory<Node>(node, 0, visible)
		);
	}
}
