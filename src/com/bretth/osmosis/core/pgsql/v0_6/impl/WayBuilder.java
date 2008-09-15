// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package com.bretth.osmosis.core.pgsql.v0_6.impl;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

import org.postgis.Geometry;
import org.postgis.PGgeometry;

import com.bretth.osmosis.core.OsmosisRuntimeException;
import com.bretth.osmosis.core.domain.v0_6.Way;


/**
 * Reads and writes way attributes to jdbc classes.
 * 
 * @author Brett Henderson
 */
public class WayBuilder extends EntityBuilder<Way> {
	
	private boolean supportBboxColumn;
	
	
	/**
	 * Creates a new instance.
	 */
	public WayBuilder() {
		supportBboxColumn = false;
	}


	/**
	 * Creates a new instance.
	 * 
	 * @param supportBboxColumn
	 *            If true, the bounding box column will be included in updates.
	 */
	public WayBuilder(boolean supportBboxColumn) {
		this.supportBboxColumn = supportBboxColumn;
	}
	
	
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
		if (supportBboxColumn) {
			return new String[] {"bbox"};
		} else {
			return new String[] {};
		}
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
	public int populateEntityParameters(PreparedStatement statement, int initialIndex, Way way) {
		// Populate the entity level parameters.
		return populateCommonEntityParameters(statement, initialIndex, way);
	}
	
	
	/**
	 * Sets entity values as bind variable parameters to an entity insert query.
	 * 
	 * @param statement
	 *            The prepared statement to add the values to.
	 * @param initialIndex
	 *            The offset index of the first variable to set.
	 * @param way
	 *            The entity containing the data to be inserted.
	 * @param bbox
	 *            The bounding box attribute of the way.
	 * @return The current parameter offset.
	 */
	public int populateEntityParameters(PreparedStatement statement, int initialIndex, Way way, Geometry bbox) {
		int prmIndex;
		
		prmIndex = populateEntityParameters(statement, initialIndex, way);
		
		try {
			statement.setObject(prmIndex++, new PGgeometry(bbox));
		} catch (SQLException e) {
			throw new OsmosisRuntimeException(
				"Unable to set the bbox for way " + way.getId() + ".",
				e
			);
		}
		
		return prmIndex;
	}
}
