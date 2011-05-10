// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.pgsimple.v0_6.impl;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

import org.openstreetmap.osmosis.core.OsmosisRuntimeException;
import org.openstreetmap.osmosis.core.domain.v0_6.CommonEntityData;
import org.openstreetmap.osmosis.core.domain.v0_6.Node;
import org.openstreetmap.osmosis.pgsimple.common.PointBuilder;
import org.postgis.PGgeometry;
import org.postgis.Point;


/**
 * Reads and writes node attributes to jdbc classes.
 * 
 * @author Brett Henderson
 */
public class NodeMapper extends EntityMapper<Node> {
	
	private PointBuilder pointBuilder;
	
	
	/**
	 * Creates a new instance.
	 */
	public NodeMapper() {
		pointBuilder = new PointBuilder();
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getEntityName() {
		return "node";
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public ActionDataType getEntityType() {
		return ActionDataType.NODE;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Class<Node> getEntityClass() {
		return Node.class;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected String[] getTypeSpecificFieldNames() {
		return new String[] {"geom"};
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Node parseRecord(ResultSet resultSet) {
		try {
			PGgeometry geom;
			Point point;
			
			geom = (PGgeometry) resultSet.getObject("geom");
			point = (Point) geom.getGeometry();
			
			return new Node(
				new CommonEntityData(
					resultSet.getLong("id"),
					resultSet.getInt("version"),
					new Date(resultSet.getTimestamp("tstamp").getTime()),
					buildUser(resultSet),
					resultSet.getLong("changeset_id")
				),
				point.y,
				point.x
			);
			
		} catch (SQLException e) {
			throw new OsmosisRuntimeException("Unable to build a node from the current recordset row.", e);
		}
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public int populateEntityParameters(PreparedStatement statement, int initialIndex, Node node) {
		int prmIndex;
		
		// Populate the entity level parameters.
		prmIndex = populateCommonEntityParameters(statement, initialIndex, node);
		
		try {
			// Set the node level parameters.
			statement.setObject(
					prmIndex++,
					new PGgeometry(pointBuilder.createPoint(node.getLatitude(), node.getLongitude())));
			
		} catch (SQLException e) {
			throw new OsmosisRuntimeException(
					"Unable to set a prepared statement parameter for node " + node.getId() + ".", e);
		}
		
		return prmIndex;
	}
}
