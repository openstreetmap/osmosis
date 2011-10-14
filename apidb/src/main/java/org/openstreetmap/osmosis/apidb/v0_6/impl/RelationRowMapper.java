// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.apidb.v0_6.impl;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.openstreetmap.osmosis.core.database.RowMapperListener;
import org.openstreetmap.osmosis.core.domain.v0_6.CommonEntityData;
import org.openstreetmap.osmosis.core.domain.v0_6.Relation;


/**
 * Maps relation result set rows into relation objects.
 */
public class RelationRowMapper implements RowMapperListener<CommonEntityData> {
	
	private RowMapperListener<Relation> listener;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param listener
	 *            The destination for result objects.
	 */
	public RelationRowMapper(RowMapperListener<Relation> listener) {
		this.listener = listener;
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public void process(CommonEntityData data, ResultSet resultSet) throws SQLException {
		Relation relation;
		
        relation = new Relation(data);
        
        listener.process(relation, resultSet);
	}
}
