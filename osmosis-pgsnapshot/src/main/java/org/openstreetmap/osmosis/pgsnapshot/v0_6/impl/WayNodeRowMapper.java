// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.pgsnapshot.v0_6.impl;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.openstreetmap.osmosis.core.database.DbOrderedFeature;
import org.openstreetmap.osmosis.core.domain.v0_6.WayNode;
import org.springframework.jdbc.core.RowMapper;


/**
 * Maps database rows into way node database objects.
 * 
 * @author Brett Henderson
 */
public class WayNodeRowMapper implements RowMapper<DbOrderedFeature<WayNode>> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public DbOrderedFeature<WayNode> mapRow(ResultSet rs, int rowNumber) throws SQLException {
		return new DbOrderedFeature<WayNode>(
				rs.getLong("entity_id"),
				new WayNode(
					rs.getLong("node_id")
				),
				rs.getInt("sequence_id")
			);
	}
}
