package com.bretth.osm.transformer.mysql.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

import com.bretth.osm.transformer.data.Way;
import com.bretth.osm.transformer.pipeline.PipelineRuntimeException;


public class WayReader extends EntityReader<Way> {
	private static final String SELECT_SQL =
		"SELECT id, timestamp FROM ways ORDER BY id";
	
	
	protected Way createNextValue(ResultSet resultSet) {
		long id;
		Date timestamp;
		
		try {
			id = resultSet.getLong("id");
			timestamp = resultSet.getTimestamp("timestamp");
		} catch (SQLException e) {
			throw new PipelineRuntimeException("Unable to read way fields.", e);
		}
		
		return new Way(id, timestamp);
	}
	
	
	protected String getQuerySql() {
		return SELECT_SQL;
	} 
}
