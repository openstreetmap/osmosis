// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.apidb.v0_6.impl;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.openstreetmap.osmosis.core.database.RowMapperListener;
import org.openstreetmap.osmosis.core.domain.v0_6.Entity;


/**
 * Maps entity history result set rows into entity history objects.
 * 
 * @param <T>
 *            The type of entity to be processed.
 */
public class EntityHistoryRowMapper<T extends Entity> implements RowMapperListener<T> {
	
	private RowMapperListener<EntityHistory<T>> listener;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param listener
	 *            The destination for result objects.
	 */
	public EntityHistoryRowMapper(RowMapperListener<EntityHistory<T>> listener) {
		this.listener = listener;
	}
	

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void process(T data, ResultSet resultSet) throws SQLException {
		boolean visible;
		EntityHistory<T> entityHistory;
		
		visible = resultSet.getBoolean("visible");
		
		entityHistory = new EntityHistory<T>(data, visible);
		
		listener.process(entityHistory, resultSet);
	}
}
