// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.pgsimple.v0_6.impl;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Arrays;

import org.openstreetmap.osmosis.core.OsmosisRuntimeException;
import org.openstreetmap.osmosis.core.domain.v0_6.Entity;
import org.openstreetmap.osmosis.core.domain.v0_6.OsmUser;


/**
 * Provides functionality common to all database entity builder implementations.
 * 
 * @author Brett Henderson
 * @param <T>
 *            The entity type to be supported.
 */
public abstract class EntityMapper<T extends Entity> {
	
	/**
	 * Returns the name of the entity to substitute into SQL statements. This is
	 * a low-tech way of making the queries type independent.
	 * 
	 * @return The entity name as defined in the database schema.
	 */
	public abstract String getEntityName();
	
	
	/**
	 * Returns the action data type of the entity.
	 * 
	 * @return The action type.
	 */
	public abstract ActionDataType getEntityType();
	
	
	/**
	 * Returns the class type for the entity.
	 * 
	 * @return The entity type class.
	 */
	public abstract Class<T> getEntityClass();
	
	
	/**
	 * The SQL SELECT statement for counting entities. It will return a count of
	 * matching records.
	 * 
	 * @param filterByEntityId
	 *            If true, a WHERE clause will be added filtering by the entity
	 *            id column.
	 * @return The SQL string.
	 */
	public String getSqlSelectCount(boolean filterByEntityId) {
		StringBuilder resultSql;
		
		resultSql = new StringBuilder();
		resultSql.append("SELECT Count(e.*) AS count FROM " + getEntityName() + "s e");
		if (filterByEntityId) {
			resultSql.append(" WHERE e.id = ?");
		}
		
		return resultSql.toString();
	}
	
	
	/**
	 * Produces an array of additional column names specific to this entity type
	 * to be returned by entity queries.
	 * 
	 * @return The column names.
	 */
	protected abstract String[] getTypeSpecificFieldNames();
	
	
	/**
	 * The SQL SELECT statement for retrieving entity details.
	 * 
	 * @param filterByEntityId
	 *            If true, a WHERE clause will be added filtering by the entity
	 *            id column.
	 * @param orderByEntityId
	 *            If true, an ORDER BY clause will be added ordering by the
	 *            entity id column.
	 * @return The SQL string.
	 */
	public String getSqlSelect(boolean filterByEntityId, boolean orderByEntityId) {
		StringBuilder resultSql;
		
		resultSql = new StringBuilder();
		resultSql.append("SELECT e.id, e.version, e.user_id, u.name AS user_name, e.tstamp, e.changeset_id");
		for (String fieldName : Arrays.asList(getTypeSpecificFieldNames())) {
			resultSql.append(", ").append(fieldName);
		}
		resultSql.append(" FROM ");
		resultSql.append(getEntityName());
		resultSql.append("s e");
		resultSql.append(" LEFT OUTER JOIN users u ON e.user_id = u.id");
		if (filterByEntityId) {
			resultSql.append(" WHERE e.id = ?");
		}
		if (orderByEntityId) {
			resultSql.append(" ORDER BY e.id");
		}
		
		return resultSql.toString();
	}


	/**
	 * The SQL INSERT statement for adding entities.
	 * 
	 * @param rowCount
	 *            The number of rows to insert in a single statement.
	 * @return The SQL string.
	 */
	public String getSqlInsert(int rowCount) {
		String[] typeSpecificFieldNames;
		StringBuilder resultSql;
		
		typeSpecificFieldNames = getTypeSpecificFieldNames();
		
		resultSql = new StringBuilder();
		resultSql.append("INSERT INTO ").append(getEntityName()).append("s");
		resultSql.append("(id, version, user_id, tstamp, changeset_id");
		for (String fieldName : Arrays.asList(typeSpecificFieldNames)) {
			resultSql.append(", ").append(fieldName);
		}
		resultSql.append(") VALUES ");
		for (int row = 0; row < rowCount; row++) {
			if (row > 0) {
				resultSql.append(", ");
			}
			resultSql.append("(?, ?, ?, ?, ?");
			for (int i = 0; i < typeSpecificFieldNames.length; i++) {
				resultSql.append(", ?");
			}
			resultSql.append(")");
		}
		
		return resultSql.toString();
	}
	
	
	/**
	 * The SQL UPDATE statement for updating entity details.
	 * 
	 * @param filterByEntityId
	 *            If true, a WHERE clause will be added filtering by the entity
	 *            id column.
	 * @return The SQL String.
	 */
	public String getSqlUpdate(boolean filterByEntityId) {
		StringBuilder resultSql;
		
		resultSql = new StringBuilder();
		resultSql.append("UPDATE ").append(getEntityName())
				.append("s SET id = ?, version = ?, user_id = ?, tstamp = ?, changeset_id = ?");
		for (String fieldName : Arrays.asList(getTypeSpecificFieldNames())) {
			resultSql.append(", ").append(fieldName).append(" = ?");
		}
		if (filterByEntityId) {
			resultSql.append(" WHERE id = ?");
		}
		
		return resultSql.toString();
	}
	
	
	/**
	 * The SQL UPDATE statement for logically deleting entities.
	 * 
	 * @param filterByEntityId
	 *            If true, a WHERE clause will be added filtering by the entity
	 *            id column.
	 * @return The SQL String.
	 */
	public String getSqlDelete(boolean filterByEntityId) {
		StringBuilder resultSql;
		
		resultSql = new StringBuilder();
		resultSql.append("DELETE FROM ").append(getEntityName()).append("s");
		if (filterByEntityId) {
			resultSql.append(" WHERE id = ?");
		}
		
		return resultSql.toString();
	}
	
	
	/**
	 * Creates a new entity based upon the current row in the result set.
	 * 
	 * @param resultSet
	 *            The result set to read from.
	 * @return The newly built entity object.
	 */
	public abstract T parseRecord(ResultSet resultSet);
	
	
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
			
			userId = resultSet.getInt("user_id");
			if (userId == OsmUser.NONE.getId()) {
				user = OsmUser.NONE;
			} else {
				user = new OsmUser(
					userId,
					resultSet.getString("user_name")
				);
			}
			
			return user;
			
		} catch (SQLException e) {
			throw new OsmosisRuntimeException("Unable to build a user from the current recordset row.", e);
		}
	}
	
	
	/**
	 * Sets common entity values as bind variable parameters to an entity insert
	 * query.
	 * 
	 * @param statement
	 *            The prepared statement to add the values to.
	 * @param initialIndex
	 *            The offset index of the first variable to set.
	 * @param entity
	 *            The entity containing the data to be inserted.
	 * @return The current parameter offset.
	 */
	protected int populateCommonEntityParameters(PreparedStatement statement, int initialIndex, Entity entity) {
		int prmIndex;
		
		prmIndex = initialIndex;
		
		// We can't write an entity with a null timestamp.
		if (entity.getTimestamp() == null) {
			throw new OsmosisRuntimeException(
					"Entity(" + entity.getType() + ") " + entity.getId() + " does not have a timestamp set.");
		}
		
		try {
			statement.setLong(prmIndex++, entity.getId());
			statement.setInt(prmIndex++, entity.getVersion());
			statement.setInt(prmIndex++, entity.getUser().getId());
			statement.setTimestamp(prmIndex++, new Timestamp(entity.getTimestamp().getTime()));
			statement.setLong(prmIndex++, entity.getChangesetId());
			
		} catch (SQLException e) {
			throw new OsmosisRuntimeException(
				"Unable to set a prepared statement parameter for entity("
					+ entity.getType() + ") " + entity.getId() + ".", e);
		}
		
		return prmIndex;
	}
	
	
	/**
	 * Sets entity values as bind variable parameters to an entity insert query.
	 * 
	 * @param statement
	 *            The prepared statement to add the values to.
	 * @param initialIndex
	 *            The offset index of the first variable to set.
	 * @param entity
	 *            The entity containing the data to be inserted.
	 * @return The current parameter offset.
	 */
	public abstract int populateEntityParameters(PreparedStatement statement, int initialIndex, T entity);
}
