// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package com.bretth.osmosis.core.pgsql.v0_6.impl;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

import com.bretth.osmosis.core.OsmosisRuntimeException;
import com.bretth.osmosis.core.domain.v0_6.Way;


/**
 * Reads and writes way attributes to jdbc classes.
 * 
 * @author Brett Henderson
 */
public class WayBuilder extends EntityBuilder<Way> {
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getEntityName() {
		return "way";
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Class<Way> getEntityClass() {
		return Way.class;
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
	public Way buildEntity(ResultSet resultSet) {
		try {
			return new Way(
				resultSet.getLong("id"),
				resultSet.getInt("version"),
				new Date(resultSet.getTimestamp("tstamp").getTime()),
				buildUser(resultSet)
			);
			
		} catch (SQLException e) {
			throw new OsmosisRuntimeException("Unable to build a way from the current recordset row.", e);
		}
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public int populateEntityParameters(PreparedStatement statement, int initialIndex, Way way, ChangesetAction action) {
		// Populate the entity level parameters.
		return populateCommonEntityParameters(statement, initialIndex, way, action);
	}
}
