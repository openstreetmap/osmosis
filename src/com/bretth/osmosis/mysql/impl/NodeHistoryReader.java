package com.bretth.osmosis.mysql.impl;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;

import com.bretth.osmosis.OsmosisRuntimeException;
import com.bretth.osmosis.data.Node;


/**
 * Reads node history records for nodes that have been modified within a time
 * interval. All history items will be returned for the node from node creation
 * up to the end of the time interval.
 * 
 * @author Brett Henderson
 */
public class NodeHistoryReader extends EntityReader<EntityHistory<Node>> {
	// The sub-select identifies the nodes that have been modified within the
	// time interval. The outer query then queries all node history items up to
	// the end of the time interval.
	private static final String SELECT_SQL =
		"SELECT id, timestamp, latitude, longitude, tags, visible"
		+ " FROM nodes"
		+ " WHERE id IN ("
		+ "SELECT id FROM nodes WHERE timestamp >= ? AND timestamp < ?"
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
	public NodeHistoryReader(String host, String database, String user, String password, Date intervalBegin, Date intervalEnd) {
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
	protected EntityHistory<Node> createNextValue(ResultSet resultSet) {
		long id;
		Date timestamp;
		double latitude;
		double longitude;
		String tags;
		boolean visible;
		Node node;
		EntityHistory<Node> nodeHistory;
		
		try {
			id = resultSet.getLong("id");
			timestamp = resultSet.getTimestamp("timestamp");
			latitude = resultSet.getDouble("latitude");
			longitude = resultSet.getDouble("longitude");
			tags = resultSet.getString("tags");
			visible = resultSet.getBoolean("visible");
			
		} catch (SQLException e) {
			throw new OsmosisRuntimeException("Unable to read node fields.", e);
		}
		
		node = new Node(id, timestamp, latitude, longitude);
		node.addTags(tagParser.parseTags(tags));
		
		nodeHistory = new EntityHistory<Node>(node, 0, visible);
		
		return nodeHistory;
	}
}
