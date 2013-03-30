// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.pgsnapshot.v0_6.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Date;
import java.util.Map.Entry;

import org.openstreetmap.osmosis.core.OsmosisRuntimeException;
import org.openstreetmap.osmosis.core.domain.v0_6.CommonEntityData;
import org.openstreetmap.osmosis.core.domain.v0_6.Entity;
import org.openstreetmap.osmosis.core.domain.v0_6.OsmUser;
import org.openstreetmap.osmosis.core.domain.v0_6.Tag;
import org.openstreetmap.osmosis.hstore.PGHStore;
import org.springframework.jdbc.core.RowMapper;


/**
 * Maps database rows into Entity objects.
 * 
 * @author Brett Henderson
 * @param <T>
 *            The entity type to be supported.
 */
public abstract class EntityRowMapper<T extends Entity> implements RowMapper<T> {
	
	/**
	 * Creates a new user record based upon the current result set row.
	 * 
	 * @param resultSet
	 *            The result set to read from.
	 * @return The newly build user object.
	 */
	private OsmUser buildUser(ResultSet resultSet) {
		try {
			int userId;
			OsmUser user;
			
			userId = resultSet.getInt("user_id");
			if (userId == OsmUser.NONE.getId()) {
				user = OsmUser.NONE;
			} else {
				user = new OsmUser(
					userId,
					resultSet.getString("user_name")
				);
			}
			
			return user;
			
		} catch (SQLException e) {
			throw new OsmosisRuntimeException("Unable to build a user from the current recordset row.", e);
		}
	}
	
	
	/**
	 * Retrieves the data common to all entities.
	 * 
	 * @param rs
	 *            The result set.
	 * @throws SQLException
	 *             if a database error is encountered.
	 * @return The common entity data.
	 */
	protected CommonEntityData mapCommonEntityData(ResultSet rs) throws SQLException {
		CommonEntityData entityData;
		PGHStore dbTags;
		Collection<Tag> tags;
		
		entityData = new CommonEntityData(
			rs.getLong("id"),
			rs.getInt("version"),
			new Date(rs.getTimestamp("tstamp").getTime()),
			buildUser(rs),
			rs.getLong("changeset_id")
		);
		
		dbTags = (PGHStore) rs.getObject("tags");
		if (dbTags != null) {
			tags = entityData.getTags();
			for (Entry<String, String> tagEntry : dbTags.entrySet()) {
				tags.add(new Tag(tagEntry.getKey(), tagEntry.getValue()));
			}
		}
		
		return entityData;
	}
}
