// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.apidb.v0_6.impl;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.openstreetmap.osmosis.core.database.RowMapperListener;
import org.openstreetmap.osmosis.core.domain.v0_6.CommonEntityData;
import org.openstreetmap.osmosis.core.domain.v0_6.Node;
import org.openstreetmap.osmosis.core.util.FixedPrecisionCoordinateConvertor;


/**
 * Maps node result set rows into node objects.
 */
public class NodeRowMapper implements RowMapperListener<CommonEntityData> {
	
	private RowMapperListener<Node> listener;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param listener
	 *            The destination for result objects.
	 */
	public NodeRowMapper(RowMapperListener<Node> listener) {
		this.listener = listener;
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public void process(CommonEntityData data, ResultSet resultSet) throws SQLException {
		double latitude;
		double longitude;
		Node node;
		
		latitude = FixedPrecisionCoordinateConvertor.convertToDouble(resultSet.getInt("latitude"));
        longitude = FixedPrecisionCoordinateConvertor.convertToDouble(resultSet.getInt("longitude"));
		
        node = new Node(data, latitude, longitude);
        
        listener.process(node, resultSet);
	}
}
