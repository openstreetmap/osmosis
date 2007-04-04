package com.bretth.osm.transformer.mysql.impl;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.bretth.osm.transformer.data.Segment;
import com.bretth.osm.transformer.pipeline.PipelineRuntimeException;


public class SegmentReader extends EmbeddedTagEntityReader<Segment> {
	private static final String SELECT_SQL =
		"SELECT id, node_a, node_b, tags FROM segments ORDER BY id";
	
	
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
			throw new PipelineRuntimeException("Unable to read segment fields.", e);
		}
		
		segment = new Segment(id, from, to);
		segment.addTags(parseTags(tags));
		
		return segment;
	}
	
	
	protected String getQuerySql() {
		return SELECT_SQL;
	} 
}
