// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.pgsnapshot.common;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.openstreetmap.osmosis.core.database.RowMapperListener;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.RowMapper;


/**
 * Combines the functionality of Spring RowMapper and RowCallbackHandler allowing a single RowMapper
 * implementation to be used for streaming operations.
 * 
 * @author Brett Henderson
 * 
 * @param <T>
 *            The entity type to be supported.
 */
public class RowMapperRowCallbackListener<T> implements RowCallbackHandler {

	private RowMapper<T> rowMapper;
	private RowMapperListener<T> listener;


	/**
	 * Creates a new instance.
	 * 
	 * @param rowMapper
	 *            The row mapper used to convert rows into objects.
	 * @param listener
	 *            The receiver of created records.
	 */
	public RowMapperRowCallbackListener(RowMapper<T> rowMapper, RowMapperListener<T> listener) {
		this.rowMapper = rowMapper;
		this.listener = listener;
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public void processRow(ResultSet rs) throws SQLException {
		listener.process(rowMapper.mapRow(rs, 0), rs);
	}
}
