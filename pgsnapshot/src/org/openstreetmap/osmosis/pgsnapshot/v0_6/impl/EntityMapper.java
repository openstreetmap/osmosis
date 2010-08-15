// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.pgsnapshot.v0_6.impl;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Map;

import org.openstreetmap.osmosis.core.OsmosisRuntimeException;
import org.openstreetmap.osmosis.core.domain.v0_6.Entity;
import org.openstreetmap.osmosis.core.domain.v0_6.Tag;
import org.openstreetmap.osmosis.hstore.PGHStore;
import org.springframework.jdbc.core.RowMapper;


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
	 * Returns the row mapper implementation for this entity type.
	 * 
	 * @return The row mapper.
	 */
	public abstract RowMapper<T> getRowMapper();
	
	
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
		return getSqlSelect("", filterByEntityId, orderByEntityId);
	}


	/**
	 * The SQL SELECT statement for retrieving entity details.
	 * 
	 * @param tablePrefix
	 *            The prefix for the entity table name. This allows another table to be queried if
	 *            necessary such as a temporary results table.
	 * @param filterByEntityId
	 *            If true, a WHERE clause will be added filtering by the entity id column.
	 * @param orderByEntityId
	 *            If true, an ORDER BY clause will be added ordering by the entity id column.
	 * @return The SQL string.
	 */
	public String getSqlSelect(String tablePrefix, boolean filterByEntityId, boolean orderByEntityId) {
		StringBuilder resultSql;
		
		resultSql = new StringBuilder();
		resultSql.append("SELECT e.id, e.version, e.user_id, u.name AS user_name, e.tstamp, e.changeset_id, e.tags");
		for (String fieldName : Arrays.asList(getTypeSpecificFieldNames())) {
			resultSql.append(", ").append(fieldName);
		}
		resultSql.append(" FROM ");
		resultSql.append(tablePrefix).append(getEntityName()).append("s e");
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
		resultSql.append("(id, version, user_id, tstamp, changeset_id, tags");
		for (String fieldName : Arrays.asList(typeSpecificFieldNames)) {
			resultSql.append(", ").append(fieldName);
		}
		resultSql.append(") VALUES ");
		for (int row = 0; row < rowCount; row++) {
			if (row > 0) {
				resultSql.append(", ");
			}
			resultSql.append("(:id, :version, :userId, :timestamp, :changesetId, :tags");
			for (int i = 0; i < typeSpecificFieldNames.length; i++) {
				resultSql.append(", :").append(typeSpecificFieldNames[i]);
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
				.append("s SET id = :id, version = :version, user_id = :userId,"
						+ " tstamp = :timestamp, changeset_id = :changesetId, tags = :tags");
		for (String fieldName : Arrays.asList(getTypeSpecificFieldNames())) {
			resultSql.append(", ").append(fieldName).append(" = :").append(fieldName);
		}
		if (filterByEntityId) {
			resultSql.append(" WHERE id = :id");
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
			resultSql.append(" WHERE id = :id");
		}
		
		return resultSql.toString();
	}


	/**
	 * Sets common entity values as bind variable parameters to an entity insert query.
	 * 
	 * @param args
	 *            The bind variable arguments to be updated.
	 * @param entity
	 *            The entity containing the data to be inserted.
	 */
	protected void populateCommonEntityParameters(Map<String, Object> args, Entity entity) {
		PGHStore tags;
		
		// We can't write an entity with a null timestamp.
		if (entity.getTimestamp() == null) {
			throw new OsmosisRuntimeException(
					"Entity(" + entity.getType() + ") " + entity.getId() + " does not have a timestamp set.");
		}
		
		tags = new PGHStore();
		for (Tag tag : entity.getTags()) {
			tags.put(tag.getKey(), tag.getValue());
		}
		
		args.put("id", entity.getId());
		args.put("version", entity.getVersion());
		args.put("userId", entity.getUser().getId());
		args.put("timestamp", new Timestamp(entity.getTimestamp().getTime()));
		args.put("changesetId", entity.getChangesetId());
		args.put("tags", tags);
	}
	
	
	/**
	 * Sets entity values as bind variable parameters to an entity insert query.
	 * 
	 * @param args
	 *            The bind variable arguments to be updated.
	 * @param entity
	 *            The entity containing the data to be inserted.
	 */
	public abstract void populateEntityParameters(Map<String, Object> args, T entity);
}
