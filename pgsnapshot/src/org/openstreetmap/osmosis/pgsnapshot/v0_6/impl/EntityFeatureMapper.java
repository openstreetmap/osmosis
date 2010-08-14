// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.pgsnapshot.v0_6.impl;

import java.util.Map;

import org.springframework.jdbc.core.RowMapper;


/**
 * Provides functionality common to all entity feature mapper implementations.
 * 
 * @author Brett Henderson
 * @param <T>
 *            The type of feature to be built.
 */
public abstract class EntityFeatureMapper<T> {

	/**
	 * Returns the name of the entity features entity type.
	 * 
	 * @return The parent entity name.
	 */
	public abstract String getParentEntityName();
	
	
	/**
	 * Returns the name of the entity feature. This is used for error messages.
	 * 
	 * @return The entity name.
	 */
	public abstract String getEntityName();
	
	
	/**
	 * Provides a default SQL ORDER BY clause suitable for this entity feature.
	 *  
	 * @return The ORDER BY clause.
	 */
	public String getSqlDefaultOrderBy() {
		return " ORDER BY entity_id"; 
	}
	
	
	/**
	 * Returns the row mapper implementation for this entity type.
	 * 
	 * @return The row mapper.
	 */
	public abstract RowMapper<T> getRowMapper();
	
	
	/**
	 * The SQL SELECT statement for retrieving entity feature details.
	 * 
	 * @param tablePrefix
	 *            The prefix for the entity table name. This allows another table to be queried if
	 *            necessary such as a temporary results table.
	 * @param filterByEntityId
	 *            If true, a WHERE clause will be added filtering by the entity
	 *            id column.
	 * @param orderBy
	 *            If true, a default ORDER BY clause will be added ordering by
	 *            the entity id column at a minimum and possibly other fields
	 *            depending on implementation.
	 * @return The SQL string.
	 */
	public abstract String getSqlSelect(String tablePrefix, boolean filterByEntityId, boolean orderBy);


	/**
	 * The SQL INSERT statement for adding features.
	 * 
	 * @param rowCount
	 *            The number of rows to insert in a single statement.
	 * @return The SQL string.
	 */
	public abstract String getSqlInsert(int rowCount);
	
	
	/**
	 * The SQL DELETE statement for deleting entity features.
	 * 
	 * @param filterByEntityId
	 *            If true, a WHERE clause will be added filtering by the entity
	 *            id column.
	 * @return The SQL String.
	 */
	public abstract String getSqlDelete(boolean filterByEntityId);
	
	
	/**
	 * Sets values as bind variable parameters to an insert query.
	 * 
	 * @param args
	 *            The bind variable arguments to be updated.
	 * @param feature
	 *            The entity containing the data to be inserted.
	 */
	public abstract void populateParameters(Map<String, Object> args, T feature);
}
