// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.apidb.v0_6.impl;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.openstreetmap.osmosis.core.database.RowMapperListener;
import org.openstreetmap.osmosis.core.domain.v0_6.Tag;
import org.springframework.jdbc.core.RowCallbackHandler;


/**
 * Maps tag result set rows into tag objects.
 */
public class TagRowMapper implements RowCallbackHandler {
	
	private RowMapperListener<Tag> listener;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param listener
	 *            The destination for result objects.
	 */
	public TagRowMapper(RowMapperListener<Tag> listener) {
		this.listener = listener;
	}
	

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void processRow(ResultSet resultSet) throws SQLException {
        String key;
        String value;
        Tag tag;
        
		key = resultSet.getString("k");
		value = resultSet.getString("v");
		
		tag = new Tag(key, value);
		
        listener.process(tag, resultSet);
	}
}
