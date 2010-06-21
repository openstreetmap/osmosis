// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.apidb.v0_6.impl;

import java.util.Collections;
import java.util.List;

import org.openstreetmap.osmosis.core.container.v0_6.EntityContainerFactory;
import org.openstreetmap.osmosis.core.container.v0_6.NodeContainerFactory;
import org.openstreetmap.osmosis.core.database.RowMapperListener;
import org.openstreetmap.osmosis.core.domain.v0_6.CommonEntityData;
import org.openstreetmap.osmosis.core.domain.v0_6.Node;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;


/**
 * Provides access to nodes in the database.
 */
public class NodeDao extends EntityDao<Node> {
	
	private static final String[] TYPE_SPECIFIC_FIELD_NAMES = new String[] {"latitude", "longitude"}; 


	/**
	 * Creates a new instance.
	 * 
	 * @param jdbcTemplate
	 *            Used to access the database.
	 */
	public NodeDao(JdbcTemplate jdbcTemplate) {
		super(jdbcTemplate, "node");
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected RowMapperListener<CommonEntityData> getEntityRowMapper(RowMapperListener<Node> entityListener) {
		return new NodeRowMapper(entityListener);
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	protected String[] getTypeSpecificFieldNames() {
		return TYPE_SPECIFIC_FIELD_NAMES;
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	protected EntityContainerFactory<Node> getContainerFactory() {
		return new NodeContainerFactory();
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	protected List<FeatureHistoryPopulator<Node, ?, ?>> getFeatureHistoryPopulators(
			String selectedEntityTableName, MapSqlParameterSource parameterSource) {
		return Collections.emptyList();
	}
}
