// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.pgsnapshot.v0_6.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.Map;

import org.openstreetmap.osmosis.core.OsmosisRuntimeException;
import org.openstreetmap.osmosis.core.domain.v0_6.Node;
import org.openstreetmap.osmosis.pgsnapshot.common.PointBuilder;
import org.postgis.PGgeometry;
import org.postgis.Point;
import org.springframework.jdbc.core.RowMapper;


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
				resultSet.getLong("id"),
				resultSet.getInt("version"),
				new Date(resultSet.getTimestamp("tstamp").getTime()),
				buildUser(resultSet),
				resultSet.getLong("changeset_id"),
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
	public void populateEntityParameters(Map<String, Object> args, Node entity) {
		populateCommonEntityParameters(args, entity);
		
		args.put("geom", new PGgeometry(pointBuilder.createPoint(entity.getLatitude(), entity.getLongitude())));
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public RowMapper<Node> getRowMapper() {
		return new NodeRowMapper();
	}
}
