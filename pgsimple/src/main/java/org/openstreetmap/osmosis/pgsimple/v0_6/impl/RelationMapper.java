// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.pgsimple.v0_6.impl;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

import org.openstreetmap.osmosis.core.OsmosisRuntimeException;
import org.openstreetmap.osmosis.core.domain.v0_6.CommonEntityData;
import org.openstreetmap.osmosis.core.domain.v0_6.Relation;


/**
 * Reads and writes relation attributes to jdbc classes.
 * 
 * @author Brett Henderson
 */
public class RelationMapper extends EntityMapper<Relation> {
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getEntityName() {
		return "relation";
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public ActionDataType getEntityType() {
		return ActionDataType.RELATION;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Class<Relation> getEntityClass() {
		return Relation.class;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected String[] getTypeSpecificFieldNames() {
		return new String[] {};
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Relation parseRecord(ResultSet resultSet) {
		try {
			return new Relation(
				new CommonEntityData(
					resultSet.getLong("id"),
					resultSet.getInt("version"),
					new Date(resultSet.getTimestamp("tstamp").getTime()),
					buildUser(resultSet),
					resultSet.getLong("changeset_id")
				)
			);
			
		} catch (SQLException e) {
			throw new OsmosisRuntimeException("Unable to build a relation from the current recordset row.", e);
		}
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public int populateEntityParameters(PreparedStatement statement, int initialIndex, Relation relation) {
		// Populate the entity level parameters.
		return populateCommonEntityParameters(statement, initialIndex, relation);
	}
}
