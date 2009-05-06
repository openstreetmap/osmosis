// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.apidb.v0_6.impl;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;

import org.openstreetmap.osmosis.core.OsmosisRuntimeException;
import org.openstreetmap.osmosis.core.apidb.common.DatabaseContext;
import org.openstreetmap.osmosis.core.database.DatabaseLoginCredentials;
import org.openstreetmap.osmosis.core.domain.v0_6.OsmUser;
import org.openstreetmap.osmosis.core.domain.v0_6.Relation;

/**
 * Reads the set of relation changes from a database that have occurred within a time interval.
 * 
 * @author Brett Henderson
 */
public class RelationHistoryReader extends BaseEntityReader<EntityHistory<Relation>> {

    private static final String SELECT_SQL =
    	"SELECT e.id, e.version, e.timestamp, e.visible, u.data_public, u.id AS user_id, u.display_name, e.changeset_id"
            + " FROM relations e"
            + " LEFT OUTER JOIN changesets c ON e.changeset_id = c.id"
            + " LEFT OUTER JOIN users u ON c.user_id = u.id"
            + " WHERE e.timestamp > ? AND e.timestamp <= ?"
            + " ORDER BY e.id, e.version";

    private final Date intervalBegin;

    private final Date intervalEnd;

    /**
     * Creates a new instance.
     * 
     * @param loginCredentials Contains all information required to connect to the database.
     * @param readAllUsers If this flag is true, all users will be read from the database regardless
     *        of their public edits flag.
     * @param intervalBegin Marks the beginning (inclusive) of the time interval to be checked.
     * @param intervalEnd Marks the end (exclusive) of the time interval to be checked.
     */
    public RelationHistoryReader(DatabaseLoginCredentials loginCredentials, boolean readAllUsers, Date intervalBegin,
            Date intervalEnd) {
        super(loginCredentials, readAllUsers);

        this.intervalBegin = intervalBegin;
        this.intervalEnd = intervalEnd;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected ResultSet createResultSet(DatabaseContext queryDbCtx) {
        try {
            PreparedStatement statement;

            statement = queryDbCtx.prepareStatementForStreaming(SELECT_SQL);
            statement.setTimestamp(1, new Timestamp(intervalBegin.getTime()));
            statement.setTimestamp(2, new Timestamp(intervalEnd.getTime()));

            return statement.executeQuery();

        } catch (SQLException e) {
            throw new OsmosisRuntimeException("Unable to create streaming resultset.", e);
        }
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
        long changesetId;

        try {
            id = resultSet.getLong("id");
            version = resultSet.getInt("version");
            timestamp = new Date(resultSet.getTimestamp("timestamp").getTime());
            visible = resultSet.getBoolean("visible");
            user = readUserField(resultSet.getBoolean("data_public"), resultSet.getInt("user_id"), resultSet
                    .getString("display_name"));
            changesetId = resultSet.getLong("changeset_id");

        } catch (SQLException e) {
            throw new OsmosisRuntimeException("Unable to read relation fields.", e);
        }

        return new ReadResult<EntityHistory<Relation>>(true, new EntityHistory<Relation>(new Relation(id, version,
                timestamp, user, changesetId), visible));
    }
}
