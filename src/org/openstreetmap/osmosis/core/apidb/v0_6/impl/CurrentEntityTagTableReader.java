// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.apidb.v0_6.impl;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.openstreetmap.osmosis.core.OsmosisRuntimeException;
import org.openstreetmap.osmosis.core.apidb.common.BaseTableReader;
import org.openstreetmap.osmosis.core.apidb.common.DatabaseContext;
import org.openstreetmap.osmosis.core.database.DatabaseLoginCredentials;
import org.openstreetmap.osmosis.core.domain.v0_6.Tag;

/**
 * Reads current tags for an entity from a tag table ordered by the entity identifier. This relies
 * on the fact that all tag tables have an identical layout.
 * 
 * @author Brett Henderson
 */
public class CurrentEntityTagTableReader extends BaseTableReader<DbFeature<Tag>> {

    private static final String SELECT_SQL_1 = "SELECT id AS entity_id, k, v FROM ";

    private static final String SELECT_SQL_2 = " ORDER BY id";

    private final String tableName;

    /**
     * Creates a new instance.
     * 
     * @param loginCredentials Contains all information required to connect to the database.
     * @param tableName The name of the table to query tag information from.
     */
    public CurrentEntityTagTableReader(DatabaseLoginCredentials loginCredentials, String tableName) {
        super(loginCredentials);

        this.tableName = tableName;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected ResultSet createResultSet(DatabaseContext queryDbCtx) {
        return queryDbCtx.executeQuery(SELECT_SQL_1 + tableName + SELECT_SQL_2);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected ReadResult<DbFeature<Tag>> createNextValue(ResultSet resultSet) {
        long entityId;
        String key;
        String value;

        try {
            entityId = resultSet.getLong("entity_id");
            key = resultSet.getString("k");
            value = resultSet.getString("v");

        } catch (SQLException e) {
            throw new OsmosisRuntimeException("Unable to read entity tag fields from table " + tableName + ".", e);
        }

        return new ReadResult<DbFeature<Tag>>(true, new DbFeature<Tag>(entityId, new Tag(key, value)));
    }
}
