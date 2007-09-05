package com.bretth.osmosis.core.mysql.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

import com.bretth.osmosis.core.OsmosisRuntimeException;
import com.bretth.osmosis.core.domain.v0_4.Node;


/**
 * Reads current nodes from a database ordered by their identifier.
 * 
 * @author Brett Henderson
 */
public class CurrentNodeReader extends BaseEntityReader<Node> {
	private static final String SELECT_SQL =
		"SELECT n.id, n.timestamp, u.data_public, u.display_name, n.latitude, n.longitude, n.tags, n.visible"
		+ " FROM current_nodes n"
		+ " INNER JOIN users u ON n.user_id = u.id"
		+ " ORDER BY n.id";
	
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
	public CurrentNodeReader(String host, String database, String user, String password, boolean readAllUsers) {
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
	protected ReadResult<Node> createNextValue(ResultSet resultSet) {
		long id;
		Date timestamp;
		String userName;
		double latitude;
		double longitude;
		String tags;
		boolean visible;
		Node node;
		
		try {
			id = resultSet.getLong("id");
			timestamp = new Date(resultSet.getTimestamp("timestamp").getTime());
			userName = readUserField(
				resultSet.getBoolean("data_public"),
				resultSet.getString("display_name")
			);
			latitude = resultSet.getDouble("latitude");
			longitude = resultSet.getDouble("longitude");
			tags = resultSet.getString("tags");
			visible = resultSet.getBoolean("visible");
			
		} catch (SQLException e) {
			throw new OsmosisRuntimeException("Unable to read node fields.", e);
		}
		
		node = new Node(id, timestamp, userName, latitude, longitude);
		node.addTags(tagParser.parseTags(tags));
		
		// Non-visible records will be ignored by the caller.
		return new ReadResult<Node>(
			visible,
			node
		);
	}
}
