package com.bretth.osmosis.mysql.impl;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;

import com.bretth.osmosis.OsmosisRuntimeException;
import com.bretth.osmosis.data.Node;


/**
 * Reads all node history items for a single node from a database ordered by
 * their identifier.
 * 
 * @author Brett Henderson
 */
public class NodeHistoryReader extends EntityReader<NodeHistory> {
	private static final String SELECT_SQL =
		"SELECT id, timestamp, latitude, longitude, tags, visible FROM nodes WHERE id = ? AND timestamp < ? ORDER BY timestamp";
	
	private EmbeddedTagParser tagParser;
	private Long nodeId;
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
	public NodeHistoryReader(String host, String database, String user, String password) {
		super(host, database, user, password);
		
		tagParser = new EmbeddedTagParser();
	}
	
	
	/**
	 * Specifies the id of the node to query history for.
	 * 
	 * @param nodeId
	 *            The node identifier.
	 */
	public void setNodeId(long nodeId) {
		this.nodeId = nodeId;
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
			
			statement.setLong(1, nodeId);
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
	protected NodeHistory createNextValue(ResultSet resultSet) {
		long id;
		Date timestamp;
		double latitude;
		double longitude;
		String tags;
		boolean visible;
		Node node;
		NodeHistory nodeHistory;
		
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
		
		nodeHistory = new NodeHistory(node, visible);
		
		return nodeHistory;
	}
}
