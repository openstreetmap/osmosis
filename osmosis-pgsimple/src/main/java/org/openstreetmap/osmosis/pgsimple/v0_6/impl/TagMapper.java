// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.pgsimple.v0_6.impl;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.openstreetmap.osmosis.core.OsmosisRuntimeException;
import org.openstreetmap.osmosis.core.database.DbFeature;
import org.openstreetmap.osmosis.core.domain.v0_6.Tag;


/**
 * Reads and writes tags to jdbc classes.
 * 
 * @author Brett Henderson
 */
public class TagMapper extends EntityFeatureMapper<DbFeature<Tag>> {
	private String parentEntityName;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param parentEntityName
	 *            The name of the parent entity. This is used to generate SQL
	 *            statements for the correct tag table name.
	 */
	public TagMapper(String parentEntityName) {
		this.parentEntityName = parentEntityName;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getParentEntityName() {
		return parentEntityName;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getEntityName() {
		return parentEntityName + "_tags";
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getSqlSelect(boolean filterByEntityId, boolean orderBy) {
		StringBuilder resultSql;
		
		resultSql = new StringBuilder();
		resultSql.append("SELECT ").append(parentEntityName).append("_id AS entity_id, k, v FROM ");
		resultSql.append(parentEntityName).append("_tags f");
		if (filterByEntityId) {
			resultSql.append(" WHERE ").append(parentEntityName).append("_id = ?");
		}
		if (orderBy) {
			resultSql.append(getSqlDefaultOrderBy());
		}
		
		return resultSql.toString();
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getSqlInsert(int rowCount) {
		StringBuilder resultSql;
		
		resultSql = new StringBuilder();
		resultSql.append("INSERT INTO ").append(parentEntityName).append("_tags (");
		resultSql.append(parentEntityName).append("_id, k, v) VALUES ");
		for (int row = 0; row < rowCount; row++) {
			if (row > 0) {
				resultSql.append(", ");
			}
			resultSql.append("(?, ?, ?)");
		}
		
		return resultSql.toString();
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getSqlDelete(boolean filterByEntityId) {
		StringBuilder resultSql;
		
		resultSql = new StringBuilder();
		resultSql.append("DELETE FROM ").append(parentEntityName).append("_tags");
		if (filterByEntityId) {
			resultSql.append(" WHERE ").append(parentEntityName).append("_id = ?");
		}
		
		return resultSql.toString();
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public DbFeature<Tag> buildEntity(ResultSet resultSet) {
		try {
			return new DbFeature<Tag>(
				resultSet.getLong("entity_id"),
				new Tag(
					resultSet.getString("k"),
					resultSet.getString("v")
				)
			);
			
		} catch (SQLException e) {
			throw new OsmosisRuntimeException("Unable to build a tag from the current recordset row.", e);
		}
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public int populateEntityParameters(PreparedStatement statement, int initialIndex, DbFeature<Tag> entityFeature) {
		try {
			int prmIndex;
			Tag tag;
			
			tag = entityFeature.getFeature();
			
			prmIndex = initialIndex;
			
			statement.setLong(prmIndex++, entityFeature.getEntityId());
			statement.setString(prmIndex++, tag.getKey());
			statement.setString(prmIndex++, tag.getValue());
			
			return prmIndex;
			
		} catch (SQLException e) {
			throw new OsmosisRuntimeException(
				"Unable to populate tag parameters for entity "
					+ parentEntityName + " " + entityFeature.getEntityId() + "."
			);
		}
	}
}
