// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package com.bretth.osmosis.core.pgsql.v0_6.impl;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

import com.bretth.osmosis.core.OsmosisRuntimeException;
import com.bretth.osmosis.core.domain.v0_6.Relation;


/**
 * Reads and writes relation attributes to jdbc classes.
 * 
 * @author Brett Henderson
 */
public class RelationBuilder extends EntityBuilder<Relation> {
	
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
	public Relation buildEntity(ResultSet resultSet) {
		try {
			return new Relation(
				resultSet.getLong("id"),
				resultSet.getInt("version"),
				new Date(resultSet.getTimestamp("tstamp").getTime()),
				buildUser(resultSet)
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
