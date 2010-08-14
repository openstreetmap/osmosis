// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.pgsnapshot.v0_6.impl;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.openstreetmap.osmosis.core.domain.v0_6.Way;


/**
 * Maps database rows into Way objects.
 * 
 * @author Brett Henderson
 */
public class WayRowMapper extends EntityRowMapper<Way> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Way mapRow(ResultSet rs, int rowNumber) throws SQLException {
		return new Way(mapCommonEntityData(rs));
	}
}
