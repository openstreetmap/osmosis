// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.pgsimple.v0_6.impl;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.openstreetmap.osmosis.core.OsmosisRuntimeException;
import org.openstreetmap.osmosis.core.domain.v0_6.CommonEntityData;
import org.openstreetmap.osmosis.core.domain.v0_6.Way;
import org.postgis.Geometry;
import org.postgis.PGgeometry;


/**
 * Reads and writes way attributes to jdbc classes.
 * 
 * @author Brett Henderson
 */
public class WayMapper extends EntityMapper<Way> {
	
	private boolean supportBboxColumn;
	private boolean supportLinestringColumn;
	
	
	/**
	 * Creates a new instance.
	 */
	public WayMapper() {
		supportBboxColumn = false;
	}


	/**
	 * Creates a new instance.
	 * 
	 * @param supportBboxColumn
	 *            If true, the bounding box column will be included in updates.
	 * @param supportLinestringColumn
	 *            If true, the linestring column will be included in updates.
	 */
	public WayMapper(boolean supportBboxColumn, boolean supportLinestringColumn) {
		this.supportBboxColumn = supportBboxColumn;
		this.supportLinestringColumn = supportLinestringColumn;
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
	public ActionDataType getEntityType() {
		return ActionDataType.WAY;
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
		List<String> fieldNames;
		
		fieldNames = new ArrayList<String>();
		
		if (supportBboxColumn) {
			fieldNames.add("bbox");
		}
		if (supportLinestringColumn) {
			fieldNames.add("linestring");
		}
		
		return fieldNames.toArray(new String[]{});
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Way parseRecord(ResultSet resultSet) {
		try {
			return new Way(
				new CommonEntityData(
					resultSet.getLong("id"),
					resultSet.getInt("version"),
					new Date(resultSet.getTimestamp("tstamp").getTime()),
					buildUser(resultSet),
					resultSet.getLong("changeset_id")
				)
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
	 * @param geometries
	 *            The geometries to store against the way.
	 * @return The current parameter offset.
	 */
	public int populateEntityParameters(
			PreparedStatement statement, int initialIndex, Way way, List<Geometry> geometries) {
		int prmIndex;
		
		prmIndex = populateEntityParameters(statement, initialIndex, way);
		
		try {
			for (int i = 0; i < geometries.size(); i++) {
				statement.setObject(prmIndex++, new PGgeometry(geometries.get(i)));
			}
			
		} catch (SQLException e) {
			throw new OsmosisRuntimeException(
				"Unable to set the bbox for way " + way.getId() + ".",
				e
			);
		}
		
		return prmIndex;
	}
}
