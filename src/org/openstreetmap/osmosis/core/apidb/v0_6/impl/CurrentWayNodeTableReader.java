// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.apidb.v0_6.impl;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.openstreetmap.osmosis.core.OsmosisRuntimeException;
import org.openstreetmap.osmosis.core.apidb.common.BaseTableReader;
import org.openstreetmap.osmosis.core.apidb.common.DatabaseContext;
import org.openstreetmap.osmosis.core.database.DatabaseLoginCredentials;
import org.openstreetmap.osmosis.core.domain.v0_6.WayNode;

/**
 * Reads current way nodes from a database ordered by the way identifier but not by the sequence.
 * 
 * @author Brett Henderson
 */
public class CurrentWayNodeTableReader extends BaseTableReader<DbOrderedFeature<WayNode>> {

    private static final String SELECT_SQL = "SELECT id AS way_id, node_id, sequence_id" + " FROM current_way_nodes"
            + " ORDER BY id";

    /**
     * Creates a new instance.
     * 
     * @param loginCredentials Contains all information required to connect to the database.
     */
    public CurrentWayNodeTableReader(DatabaseLoginCredentials loginCredentials) {
        super(loginCredentials);
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
    protected ReadResult<DbOrderedFeature<WayNode>> createNextValue(ResultSet resultSet) {
        long wayId;
        long nodeId;
        int sequenceId;

        try {
            wayId = resultSet.getLong("way_id");
            nodeId = resultSet.getLong("node_id");
            sequenceId = resultSet.getInt("sequence_id");

        } catch (SQLException e) {
            throw new OsmosisRuntimeException("Unable to read way node fields.", e);
        }

        return new ReadResult<DbOrderedFeature<WayNode>>(true, new DbOrderedFeature<WayNode>(wayId,
                new WayNode(nodeId), sequenceId));
    }
}
