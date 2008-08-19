package com.bretth.osmosis.core.pgsql.v0_6.impl;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.bretth.osmosis.core.OsmosisRuntimeException;
import com.bretth.osmosis.core.domain.v0_6.Entity;
import com.bretth.osmosis.core.domain.v0_6.OsmUser;


/**
 * Provides functionality common to all entity builder implementations.
 * 
 * @author Brett Henderson
 * @param <T>
 *            The type of entity to be built.
 */
public abstract class EntityBuilder<T extends Entity> {
	

	/**
	 * The resultset user id field.
	 */
	private static final String FIELD_USER_ID = "user_id";
	
	/**
	 * The resultset user name field.
	 */
	private static final String FIELD_USER_NAME = "user_name";
	
	
	/**
	 * Provides the base SQL query to return rows from the entity table.
	 * 
	 * @return The base SQL query.
	 */
	public abstract String getBaseSql();
	
	
	/**
	 * Creates a new entity based upon the current row in the result set.
	 * 
	 * @param resultSet
	 *            The result set to read from.
	 * @return The newly built entity object.
	 */
	public abstract T buildEntity(ResultSet resultSet);
	
	
	/**
	 * Creates a new user record based upon the current result set row.
	 * 
	 * @param resultSet
	 *            The result set to read from.
	 * @return The newly build user object.
	 */
	protected OsmUser buildUser(ResultSet resultSet) {
		try {
			int userId;
			OsmUser user;
			
			userId = resultSet.getInt(FIELD_USER_ID);
			if (userId == OsmUser.NONE.getId()) {
				user = OsmUser.NONE;
			} else {
				user = new OsmUser(
					userId,
					resultSet.getString(FIELD_USER_NAME)
				);
			}
			
			return user;
			
		} catch (SQLException e) {
			throw new OsmosisRuntimeException("Unable to build a user from the current recordset row.", e);
		}
	}
}
