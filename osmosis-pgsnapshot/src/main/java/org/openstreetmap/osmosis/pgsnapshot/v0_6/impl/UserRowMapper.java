// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.pgsnapshot.v0_6.impl;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.openstreetmap.osmosis.core.domain.v0_6.OsmUser;
import org.springframework.jdbc.core.RowMapper;


/**
 * Maps database rows into User objects.
 * 
 * @author Brett Henderson
 */
public class UserRowMapper implements RowMapper<OsmUser> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public OsmUser mapRow(ResultSet rs, int rowNumber) throws SQLException {
		return new OsmUser(rs.getInt("id"), rs.getString("name"));
	}
}
