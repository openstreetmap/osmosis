// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.apidb.v0_6.impl;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.openstreetmap.osmosis.core.database.RowMapperListener;
import org.openstreetmap.osmosis.core.domain.v0_6.CommonEntityData;
import org.openstreetmap.osmosis.core.domain.v0_6.Way;


/**
 * Maps way result set rows into way objects.
 */
public class WayRowMapper implements RowMapperListener<CommonEntityData> {
	
	private RowMapperListener<Way> listener;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param listener
	 *            The destination for result objects.
	 */
	public WayRowMapper(RowMapperListener<Way> listener) {
		this.listener = listener;
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public void process(CommonEntityData data, ResultSet resultSet) throws SQLException {
		Way way;
		
        way = new Way(data);
        
        listener.process(way, resultSet);
	}
}
