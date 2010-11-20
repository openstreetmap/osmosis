// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.pgsimple.v0_6.impl;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.openstreetmap.osmosis.core.OsmosisRuntimeException;
import org.openstreetmap.osmosis.core.database.DbOrderedFeature;
import org.openstreetmap.osmosis.core.domain.v0_6.WayNode;


/**
 * Reads and writes way nodes to jdbc classes.
 * 
 * @author Brett Henderson
 */
public class WayNodeMapper extends EntityFeatureMapper<DbOrderedFeature<WayNode>> {
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getParentEntityName() {
		return "way";
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getEntityName() {
		return "way_nodes";
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getSqlSelect(boolean filterByEntityId, boolean orderBy) {
		StringBuilder resultSql;
		
		resultSql = new StringBuilder();
		resultSql.append("SELECT way_id AS entity_id, node_id, sequence_id FROM ");
		resultSql.append("way_nodes f");
		if (filterByEntityId) {
			resultSql.append(" WHERE entity_id = ?");
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
	public String getSqlDefaultOrderBy() {
		return super.getSqlDefaultOrderBy() + ", sequence_id";
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getSqlInsert(int rowCount) {
		StringBuilder resultSql;
		
		resultSql = new StringBuilder();
		resultSql.append("INSERT INTO way_nodes (");
		resultSql.append("way_id, node_id, sequence_id) VALUES ");
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
		resultSql.append("DELETE FROM way_nodes");
		if (filterByEntityId) {
			resultSql.append(" WHERE ").append("way_id = ?");
		}
		
		return resultSql.toString();
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public DbOrderedFeature<WayNode> buildEntity(ResultSet resultSet) {
		try {
			return new DbOrderedFeature<WayNode>(
				resultSet.getLong("entity_id"),
				new WayNode(
					resultSet.getLong("node_id")
				),
				resultSet.getInt("sequence_id")
			);
			
		} catch (SQLException e) {
			throw new OsmosisRuntimeException("Unable to build a way node from the current recordset row.", e);
		}
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public int populateEntityParameters(
			PreparedStatement statement, int initialIndex, DbOrderedFeature<WayNode> entityFeature) {
		try {
			int prmIndex;
			WayNode wayNode;
			
			wayNode = entityFeature.getFeature();
			
			prmIndex = initialIndex;
			
			statement.setLong(prmIndex++, entityFeature.getEntityId());
			statement.setLong(prmIndex++, wayNode.getNodeId());
			statement.setInt(prmIndex++, entityFeature.getSequenceId());
			
			return prmIndex;
			
		} catch (SQLException e) {
			throw new OsmosisRuntimeException(
				"Unable to populate way node parameters for way "
					+ entityFeature.getEntityId() + ".",
				e
			);
		}
	}
}
