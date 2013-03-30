// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.pgsnapshot.v0_6.impl;

import java.util.Map;

import org.openstreetmap.osmosis.core.domain.v0_6.Node;
import org.openstreetmap.osmosis.pgsnapshot.common.PointBuilder;
import org.postgis.PGgeometry;
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
