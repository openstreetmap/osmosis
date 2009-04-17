// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package org.openstreetmap.osmosis.core.apidb.v0_6.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

import org.openstreetmap.osmosis.core.OsmosisRuntimeException;
import org.openstreetmap.osmosis.core.apidb.common.DatabaseContext;
import org.openstreetmap.osmosis.core.database.DatabaseLoginCredentials;
import org.openstreetmap.osmosis.core.domain.v0_6.OsmUser;
import org.openstreetmap.osmosis.core.domain.v0_6.Relation;

/**
 * Reads all relations from a database ordered by their identifier. These relations won't be
 * populated with nodes and tags.
 * 
 * @author Brett Henderson
 */
public class RelationTableReader extends BaseEntityReader<EntityHistory<Relation>> {

    private static final String SELECT_SQL = "SELECT r.id, r.version, r.timestamp, r.visible, u.data_public, u.id AS user_id, u.display_name"
            + " FROM relations r"
            + " LEFT OUTER JOIN changesets c ON r.changeset_id = c.id"
            + " LEFT OUTER JOIN users u ON c.user_id = u.id" + " ORDER BY r.id, r.version";

    /**
     * Creates a new instance.
     * 
     * @param loginCredentials Contains all information required to connect to the database.
     * @param readAllUsers If this flag is true, all users will be read from the database regardless
     *        of their public edits flag.
     */
    public RelationTableReader(DatabaseLoginCredentials loginCredentials, boolean readAllUsers) {
        super(loginCredentials, readAllUsers);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected ResultSet createResultSet(DatabaseContext queryDbCtx) {
        return queryDbCtx.executeStreamingQuery(SELECT_SQL);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected ReadResult<EntityHistory<Relation>> createNextValue(ResultSet resultSet) {
        long id;
        int version;
        Date timestamp;
        boolean visible;
        OsmUser user;

        try {
            id = resultSet.getLong("id");
            version = resultSet.getInt("version");
            timestamp = new Date(resultSet.getTimestamp("timestamp").getTime());
            visible = resultSet.getBoolean("visible");
            user = readUserField(resultSet.getBoolean("data_public"), resultSet.getInt("user_id"), resultSet
                    .getString("display_name"));

        } catch (SQLException e) {
            throw new OsmosisRuntimeException("Unable to read relation fields.", e);
        }

        return new ReadResult<EntityHistory<Relation>>(true, new EntityHistory<Relation>(new Relation(id, version,
                timestamp, user), visible));
    }
}
