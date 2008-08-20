package com.bretth.osmosis.core.pgsql.v0_6.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

import com.bretth.osmosis.core.OsmosisRuntimeException;
import com.bretth.osmosis.core.domain.v0_6.Way;


/**
 * Creates ways from result set rows.
 * 
 * @author Brett Henderson
 */
public class WayBuilder extends EntityBuilder<Way> {
	/**
	 * The base SQL SELECT statement for retrieving way details.
	 */
	public static final String SQL_SELECT =
		"SELECT e.id, e.version, e.user_id, u.name AS user_name, e.tstamp" +
		" FROM ways e" +
		" LEFT OUTER JOIN users u ON e.user_id = u.id";
	
	/**
	 * The resultset id field.
	 */
	private static final String FIELD_ID = "id";
	/**
	 * The resultset version field.
	 */
	private static final String FIELD_VERSION = "version";
	/**
	 * The resultset timestamp field.
	 */
	private static final String FIELD_TIMESTAMP = "tstamp";
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getBaseSql() {
		return SQL_SELECT;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Way buildEntity(ResultSet resultSet) {
		try {
			return new Way(
				resultSet.getLong(FIELD_ID),
				resultSet.getInt(FIELD_VERSION),
				new Date(resultSet.getTimestamp(FIELD_TIMESTAMP).getTime()),
				buildUser(resultSet)
			);
			
		} catch (SQLException e) {
			throw new OsmosisRuntimeException("Unable to build a way from the current recordset row.", e);
		}
	}
}
