package com.bretth.osm.conduit.mysql.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

import com.bretth.osm.conduit.ConduitRuntimeException;
import com.bretth.osm.conduit.data.Way;


public class WayReader extends EntityReader<Way> {
	private static final String SELECT_SQL =
		"SELECT id, timestamp FROM ways ORDER BY id";
	
	
	public WayReader(String host, String database, String user, String password) {
		super(host, database, user, password);
	}
	
	
	protected Way createNextValue(ResultSet resultSet) {
		long id;
		Date timestamp;
		
		try {
			id = resultSet.getLong("id");
			timestamp = resultSet.getTimestamp("timestamp");
		} catch (SQLException e) {
			throw new ConduitRuntimeException("Unable to read way fields.", e);
		}
		
		return new Way(id, timestamp);
	}
	
	
	protected String getQuerySql() {
		return SELECT_SQL;
	} 
}
