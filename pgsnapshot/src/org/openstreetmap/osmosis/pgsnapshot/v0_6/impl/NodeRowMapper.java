// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.pgsnapshot.v0_6.impl;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.openstreetmap.osmosis.core.domain.v0_6.Node;
import org.postgis.PGgeometry;
import org.postgis.Point;


/**
 * Maps database rows into Node objects.
 * 
 * @author Brett Henderson
 */
public class NodeRowMapper extends EntityRowMapper<Node> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Node mapRow(ResultSet rs, int rowNumber) throws SQLException {
		PGgeometry geom;
		Point point;
		
		geom = (PGgeometry) rs.getObject("geom");
		point = (Point) geom.getGeometry();
		
		return new Node(mapCommonEntityData(rs), point.y, point.x);
	}
}
