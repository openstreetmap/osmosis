package com.bretth.osm.conduit.mysql.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

import com.bretth.osm.conduit.ConduitRuntimeException;
import com.bretth.osm.conduit.data.Node;


public class NodeReader extends EmbeddedTagEntityReader<Node> {
	private static final String SELECT_SQL =
		"SELECT id, timestamp, latitude, longitude, tags FROM nodes ORDER BY id";
	
	
	public NodeReader(String host, String database, String user, String password) {
		super(host, database, user, password);
	}
	
	
	protected Node createNextValue(ResultSet resultSet) {
		long id;
		Date timestamp;
		double latitude;
		double longitude;
		String tags;
		Node node;
		
		try {
			id = resultSet.getLong("id");
			timestamp = resultSet.getTimestamp("timestamp");
			latitude = resultSet.getDouble("latitude");
			longitude = resultSet.getDouble("longitude");
			tags = resultSet.getString("tags");
			
		} catch (SQLException e) {
			throw new ConduitRuntimeException("Unable to read node fields.", e);
		}
		
		node = new Node(id, timestamp, latitude, longitude);
		node.addTags(parseTags(tags));
		
		return node;
	}
	
	
	protected String getQuerySql() {
		return SELECT_SQL;
	} 
}
