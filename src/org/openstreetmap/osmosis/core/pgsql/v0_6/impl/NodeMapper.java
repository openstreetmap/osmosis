// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package org.openstreetmap.osmosis.core.pgsql.v0_6.impl;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

import org.postgis.PGgeometry;
import org.postgis.Point;

import org.openstreetmap.osmosis.core.OsmosisRuntimeException;
import org.openstreetmap.osmosis.core.domain.v0_6.Node;
import org.openstreetmap.osmosis.core.domain.v0_6.NodeBuilder;
import org.openstreetmap.osmosis.core.pgsql.common.PointBuilder;


/**
 * Reads and writes node attributes to jdbc classes.
 * 
 * @author Brett Henderson
 */
public class NodeMapper extends EntityMapper<Node, NodeBuilder> {
	
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
	public Class<NodeBuilder> getBuilderClass() {
		return NodeBuilder.class;
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
	public NodeBuilder parseRecord(ResultSet resultSet) {
		try {
			PGgeometry geom;
			Point point;
			
			geom = (PGgeometry) resultSet.getObject("geom");
			point = (Point) geom.getGeometry();
			
			return new NodeBuilder(
				resultSet.getLong("id"),
				resultSet.getInt("version"),
				new Date(resultSet.getTimestamp("tstamp").getTime()),
				buildUser(resultSet),
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
			statement.setObject(prmIndex++, new PGgeometry(pointBuilder.createPoint(node.getLatitude(), node.getLongitude())));
			
		} catch (SQLException e) {
			throw new OsmosisRuntimeException("Unable to set a prepared statement parameter for node " + node.getId() + ".", e);
		}
		
		return prmIndex;
	}
}
