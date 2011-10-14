// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.pgsnapshot.v0_6.impl;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.openstreetmap.osmosis.core.domain.v0_6.Relation;


/**
 * Maps database rows into Relation objects.
 * 
 * @author Brett Henderson
 */
public class RelationRowMapper extends EntityRowMapper<Relation> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Relation mapRow(ResultSet rs, int rowNumber) throws SQLException {
		return new Relation(mapCommonEntityData(rs));
	}
}
