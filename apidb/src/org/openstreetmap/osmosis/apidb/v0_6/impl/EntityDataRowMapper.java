// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.apidb.v0_6.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

import org.openstreetmap.osmosis.core.database.RowMapperListener;
import org.openstreetmap.osmosis.core.domain.v0_6.CommonEntityData;
import org.openstreetmap.osmosis.core.domain.v0_6.OsmUser;
import org.springframework.jdbc.core.RowCallbackHandler;


/**
 * Maps entity result set rows into common entity data objects.
 */
public class EntityDataRowMapper implements RowCallbackHandler {
	
	private RowMapperListener<CommonEntityData> listener;
	private boolean readAllUsers;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param listener
	 *            The destination for result objects.
	 * @param readAllUsers
	 *            If true, even anonymous users will be returned. Should be false in most cases.
	 */
	public EntityDataRowMapper(RowMapperListener<CommonEntityData> listener, boolean readAllUsers) {
		this.listener = listener;
		this.readAllUsers = readAllUsers;
	}


    /**
	 * Determines the appropriate user name to add to an entity based upon the user details
	 * provided.
	 * 
	 * @param dataPublic
	 *            The value of the public edit flag for the user.
	 * @param userId
	 *            The unique id of the user.
	 * @param userName
	 *            The display name of the user.
	 * @return The appropriate user to add to the entity.
	 */
    private OsmUser readUserField(boolean dataPublic, int userId, String userName) {
        if (userId == OsmUser.NONE.getId()) {
            return OsmUser.NONE;
        } else if (dataPublic || readAllUsers) {
            String correctedUserName;

            if (userName == null) {
                correctedUserName = "";
            } else {
                correctedUserName = userName;
            }

            return new OsmUser(userId, correctedUserName);
        } else {
            return OsmUser.NONE;
        }
    }
	

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void processRow(ResultSet resultSet) throws SQLException {
        long id;
        int version;
        Date timestamp;
        OsmUser user;
        long changesetId;
        CommonEntityData entityData;
        
		id = resultSet.getLong("id");
        version = resultSet.getInt("version");
        timestamp = new Date(resultSet.getTimestamp("timestamp").getTime());
        user = readUserField(resultSet.getBoolean("data_public"), resultSet.getInt("user_id"), resultSet
                .getString("display_name"));
        changesetId = resultSet.getLong("changeset_id");
        
        //node = new Node(id, version, timestamp, user, changesetId, latitude, longitude);
        entityData = new CommonEntityData(id, version, timestamp, user, changesetId);
        
        listener.process(entityData, resultSet);
	}
}
