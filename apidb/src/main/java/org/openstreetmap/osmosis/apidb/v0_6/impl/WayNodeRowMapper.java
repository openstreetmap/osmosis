// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.apidb.v0_6.impl;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.openstreetmap.osmosis.core.database.RowMapperListener;
import org.openstreetmap.osmosis.core.domain.v0_6.WayNode;
import org.springframework.jdbc.core.RowCallbackHandler;


/**
 * Maps way node result set rows into way node objects.
 */
public class WayNodeRowMapper implements RowCallbackHandler {
	
	private RowMapperListener<WayNode> listener;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param listener
	 *            The destination for result objects.
	 */
	public WayNodeRowMapper(RowMapperListener<WayNode> listener) {
		this.listener = listener;
	}
	

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void processRow(ResultSet resultSet) throws SQLException {
        long nodeId;
        WayNode wayNode;
        
		nodeId = resultSet.getLong("node_id");
		
		wayNode = new WayNode(nodeId);
		
        listener.process(wayNode, resultSet);
	}
}
