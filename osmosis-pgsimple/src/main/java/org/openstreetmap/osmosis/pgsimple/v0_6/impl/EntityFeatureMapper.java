// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.pgsimple.v0_6.impl;

import java.sql.PreparedStatement;
import java.sql.ResultSet;


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
	 * The SQL SELECT statement for retrieving entity feature details.
	 * 
	 * @param filterByEntityId
	 *            If true, a WHERE clause will be added filtering by the entity
	 *            id column.
	 * @param orderBy
	 *            If true, a default ORDER BY clause will be added ordering by
	 *            the entity id column at a minimum and possibly other fields
	 *            depending on implementation.
	 * @return The SQL string.
	 */
	public abstract String getSqlSelect(boolean filterByEntityId, boolean orderBy);


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
	 * Creates a new entity based upon the current row in the result set.
	 * 
	 * @param resultSet
	 *            The result set to read from.
	 * @return The newly built entity object.
	 */
	public abstract T buildEntity(ResultSet resultSet);
	
	
	/**
	 * Sets entity values as bind variable parameters to an entity insert query.
	 * 
	 * @param statement
	 *            The prepared statement to add the values to.
	 * @param initialIndex
	 *            The offset index of the first variable to set.
	 * @param entityFeature
	 *            The entity containing the data to be inserted.
	 * @return The current parameter offset.
	 */
	public abstract int populateEntityParameters(PreparedStatement statement, int initialIndex, T entityFeature);
}
