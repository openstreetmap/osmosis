// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.pgsnapshot.v0_6.impl;

import java.sql.Array;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.openstreetmap.osmosis.core.domain.v0_6.Way;
import org.openstreetmap.osmosis.core.domain.v0_6.WayNode;


/**
 * Maps database rows into Way objects.
 * 
 * @author Brett Henderson
 */
public class WayRowMapper extends EntityRowMapper<Way> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Way mapRow(ResultSet rs, int rowNumber) throws SQLException {
		Way way;
		Array nodeIdArray;
		Long[] nodeIds;
		List<WayNode> wayNodes;
		
		way = new Way(mapCommonEntityData(rs));
		
		nodeIdArray = rs.getArray("nodes");
		
		if (nodeIdArray != null) {
			nodeIds = (Long[]) nodeIdArray.getArray();
			wayNodes = way.getWayNodes();
			for (long nodeId : nodeIds) {
				wayNodes.add(new WayNode(nodeId));
			}
		}
		
		return way;
	}
}
