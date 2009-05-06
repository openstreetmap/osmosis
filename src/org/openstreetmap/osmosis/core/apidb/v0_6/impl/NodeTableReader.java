// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.apidb.v0_6.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

import org.openstreetmap.osmosis.core.OsmosisRuntimeException;
import org.openstreetmap.osmosis.core.apidb.common.DatabaseContext;
import org.openstreetmap.osmosis.core.database.DatabaseLoginCredentials;
import org.openstreetmap.osmosis.core.domain.v0_6.Node;
import org.openstreetmap.osmosis.core.domain.v0_6.OsmUser;
import org.openstreetmap.osmosis.core.util.FixedPrecisionCoordinateConvertor;

/**
 * Reads all nodes from a database ordered by their identifier. These nodes won't be populated with
 * nodes and tags.
 * 
 * @author Brett Henderson
 */
public class NodeTableReader extends BaseEntityReader<EntityHistory<Node>> {

    private static final String SELECT_SQL = "SELECT n.id, n.version, n.timestamp, n.visible, u.data_public,"
            + " u.id AS user_id, u.display_name, n.changeset_id, n.latitude, n.longitude" + " FROM nodes n"
            + " LEFT OUTER JOIN changesets c ON n.changeset_id = c.id" + " LEFT OUTER JOIN users u ON c.user_id = u.id"
            + " ORDER BY n.id, n.version";

    /**
     * Creates a new instance.
     * 
     * @param loginCredentials Contains all information required to connect to the database.
     * @param readAllUsers If this flag is true, all users will be read from the database regardless
     *        of their public edits flag.
     */
    public NodeTableReader(DatabaseLoginCredentials loginCredentials, boolean readAllUsers) {
        super(loginCredentials, readAllUsers);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected ResultSet createResultSet(DatabaseContext queryDbCtx) {
        return queryDbCtx.executeQuery(SELECT_SQL);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected ReadResult<EntityHistory<Node>> createNextValue(ResultSet resultSet) {
        long id;
        int version;
        Date timestamp;
        boolean visible;
        OsmUser user;
        long changesetId;
        double latitude;
        double longitude;

        try {
            id = resultSet.getLong("id");
            version = resultSet.getInt("version");
            timestamp = new Date(resultSet.getTimestamp("timestamp").getTime());
            visible = resultSet.getBoolean("visible");
            user = readUserField(resultSet.getBoolean("data_public"), resultSet.getInt("user_id"), resultSet
                    .getString("display_name"));
            changesetId = resultSet.getLong("changeset_id");
            latitude = FixedPrecisionCoordinateConvertor.convertToDouble(resultSet.getInt("latitude"));
            longitude = FixedPrecisionCoordinateConvertor.convertToDouble(resultSet.getInt("longitude"));

        } catch (SQLException e) {
            throw new OsmosisRuntimeException("Unable to read node fields.", e);
        }

        return new ReadResult<EntityHistory<Node>>(true, new EntityHistory<Node>(new Node(id, version, timestamp, user,
                changesetId, latitude, longitude), visible));
    }
}
