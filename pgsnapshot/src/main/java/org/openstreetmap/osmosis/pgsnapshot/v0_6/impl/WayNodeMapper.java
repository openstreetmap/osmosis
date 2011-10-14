// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.pgsnapshot.v0_6.impl;

import java.util.Map;

import org.openstreetmap.osmosis.core.database.DbOrderedFeature;
import org.openstreetmap.osmosis.core.domain.v0_6.WayNode;
import org.springframework.jdbc.core.RowMapper;


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
	public String getSqlSelect(String tablePrefix, boolean filterByEntityId, boolean orderBy) {
		StringBuilder resultSql;
		
		resultSql = new StringBuilder();
		resultSql.append("SELECT way_id AS entity_id, node_id, sequence_id FROM ");
		resultSql.append("way_nodes f");
		if (!tablePrefix.isEmpty()) {
			resultSql.append(" INNER JOIN ").append(tablePrefix).append(getParentEntityName())
				.append("s e ON f.").append(getParentEntityName()).append("_id = e.id");
		}
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
			resultSql.append("(:wayId, :nodeId, :sequenceId)");
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
	public void populateParameters(Map<String, Object> args, DbOrderedFeature<WayNode> feature) {
		args.put("wayId", feature.getEntityId());
		args.put("nodeId", feature.getFeature().getNodeId());
		args.put("sequenceId", feature.getSequenceId());
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public RowMapper<DbOrderedFeature<WayNode>> getRowMapper() {
		return new WayNodeRowMapper();
	}
}
