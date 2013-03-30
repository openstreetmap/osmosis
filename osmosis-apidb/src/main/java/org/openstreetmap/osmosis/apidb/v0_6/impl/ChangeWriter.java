// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.apidb.v0_6.impl;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.openstreetmap.osmosis.core.OsmosisRuntimeException;
import org.openstreetmap.osmosis.apidb.common.DatabaseContext;
import org.openstreetmap.osmosis.core.database.DatabaseLoginCredentials;
import org.openstreetmap.osmosis.core.database.ReleasableStatementContainer;
import org.openstreetmap.osmosis.core.domain.v0_6.Node;
import org.openstreetmap.osmosis.core.domain.v0_6.Relation;
import org.openstreetmap.osmosis.core.domain.v0_6.RelationMember;
import org.openstreetmap.osmosis.core.domain.v0_6.Tag;
import org.openstreetmap.osmosis.core.domain.v0_6.Way;
import org.openstreetmap.osmosis.core.domain.v0_6.WayNode;
import org.openstreetmap.osmosis.core.task.common.ChangeAction;
import org.openstreetmap.osmosis.core.util.FixedPrecisionCoordinateConvertor;
import org.openstreetmap.osmosis.core.util.TileCalculator;


/**
 * Writes changes to a database.
 * 
 * @author Brett Henderson
 */
public class ChangeWriter {

    private static final Logger LOG = Logger.getLogger(ChangeWriter.class.getName());

    private static final String INSERT_SQL_NODE =
    	"INSERT INTO nodes (node_id, version, timestamp, visible, changeset_id, latitude, longitude, tile)"
            + " VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

    private static final String UPDATE_SQL_NODE =
    	"UPDATE nodes SET timestamp = ?, visible = ?, changeset_id = ?, latitude = ?, longitude = ?, tile = ?"
            + " WHERE node_id = ? AND version = ?";

    private static final String SELECT_SQL_NODE_COUNT =
    	"SELECT Count(node_id) AS rowCount FROM nodes WHERE node_id = ? AND version = ?";

    private static final String INSERT_SQL_NODE_CURRENT =
    	"INSERT INTO current_nodes (id, version, timestamp, visible, changeset_id, latitude, longitude, tile)"
            + " VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

    private static final String UPDATE_SQL_NODE_CURRENT =
    	"UPDATE current_nodes SET version = ?, timestamp = ?, visible = ?, changeset_id = ?, latitude = ?,"
            + " longitude = ?, tile = ? WHERE id = ?";

    private static final String SELECT_SQL_NODE_CURRENT_COUNT =
    	"SELECT Count(id) AS rowCount FROM current_nodes WHERE id = ?";

    private static final String INSERT_SQL_NODE_TAG =
	"INSERT INTO node_tags (node_id, version, k, v) VALUES (?, ?, ?, ?)";

    private static final String DELETE_SQL_NODE_TAG = "DELETE FROM node_tags WHERE node_id = ? AND version = ?";

    private static final String INSERT_SQL_NODE_TAG_CURRENT =
    	"INSERT INTO current_node_tags (node_id, k, v) VALUES (?, ?, ?)";

    private static final String DELETE_SQL_NODE_TAG_CURRENT = "DELETE FROM current_node_tags WHERE node_id = ?";

    private static final String INSERT_SQL_WAY =
    	"INSERT INTO ways (way_id, version, timestamp, visible, changeset_id) VALUES (?, ?, ?, ?, ?)";

    private static final String UPDATE_SQL_WAY =
    	"UPDATE current_ways SET timestamp = ?, visible = ?, changeset_id = ? WHERE id = ? AND version = ?";

    private static final String SELECT_SQL_WAY_COUNT =
    	"SELECT Count(way_id) AS rowCount FROM ways WHERE way_id = ? AND version = ?";

    private static final String INSERT_SQL_WAY_CURRENT =
    	"INSERT INTO current_ways (id, version, timestamp, visible, changeset_id) VALUES (?, ?, ?, ?, ?)";

    private static final String UPDATE_SQL_WAY_CURRENT =
    	"UPDATE current_ways SET version = ?, timestamp = ?, visible = ?, changeset_id = ? WHERE id = ?";

    private static final String SELECT_SQL_WAY_CURRENT_COUNT =
    	"SELECT Count(id) AS rowCount FROM current_ways WHERE id = ?";

    private static final String INSERT_SQL_WAY_TAG = "INSERT INTO way_tags (way_id, version, k, v) VALUES (?, ?, ?, ?)";

    private static final String DELETE_SQL_WAY_TAG = "DELETE FROM way_tags WHERE way_id = ? AND version = ?";

    private static final String INSERT_SQL_WAY_TAG_CURRENT =
	"INSERT INTO current_way_tags (way_id, k, v) VALUES (?, ?, ?)";

    private static final String DELETE_SQL_WAY_TAG_CURRENT = "DELETE FROM current_way_tags WHERE way_id = ?";

    private static final String INSERT_SQL_WAY_NODE =
    	"INSERT INTO way_nodes (way_id, version, node_id, sequence_id) VALUES (?, ?, ?, ?)";

    private static final String DELETE_SQL_WAY_NODE = "DELETE FROM way_nodes WHERE way_id = ? AND version = ?";

    private static final String INSERT_SQL_WAY_NODE_CURRENT =
    	"INSERT INTO current_way_nodes (way_id, node_id, sequence_id) VALUES (?, ?, ?)";

    private static final String DELETE_SQL_WAY_NODE_CURRENT = "DELETE FROM current_way_nodes WHERE way_id = ?";

    private static final String INSERT_SQL_RELATION =
    	"INSERT INTO relations (relation_id, version, timestamp, visible, changeset_id) VALUES (?, ?, ?, ?, ?)";

    private static final String UPDATE_SQL_RELATION =
    	"UPDATE relations SET timestamp = ?, visible = ?, changeset_id = ? WHERE relation_id = ? AND version = ?";

    private static final String SELECT_SQL_RELATION_COUNT =
    	"SELECT Count(id) AS rowCount FROM current_relations WHERE id = ? AND version = ?";

    private static final String INSERT_SQL_RELATION_CURRENT =
    	"INSERT INTO current_relations (id, version, timestamp, visible, changeset_id) VALUES (?, ?, ?, ?, ?)";

    private static final String UPDATE_SQL_RELATION_CURRENT =
    	"UPDATE current_relations SET version = ?, timestamp = ?, visible = ?, changeset_id = ? WHERE id = ?";

    private static final String SELECT_SQL_RELATION_CURRENT_COUNT =
    	"SELECT Count(id) AS rowCount FROM current_relations WHERE id = ?";

    private static final String INSERT_SQL_RELATION_TAG =
    	"INSERT INTO relation_tags (relation_id, version, k, v) VALUES (?, ?, ?, ?)";

    private static final String DELETE_SQL_RELATION_TAG =
	"DELETE FROM relation_tags WHERE relation_id = ? AND version = ?";

    private static final String INSERT_SQL_RELATION_TAG_CURRENT =
    	"INSERT INTO current_relation_tags (relation_id, k, v) VALUES (?, ?, ?)";

    private static final String DELETE_SQL_RELATION_TAG_CURRENT =
    	"DELETE FROM current_relation_tags WHERE relation_id = ?";

    private static final String INSERT_SQL_RELATION_MEMBER_MYSQL =
    	"INSERT INTO relation_members (relation_id, version, member_type, member_id, member_role, sequence_id)"
            + " VALUES (?, ?, ?, ?, ?, ?)";

    private static final String INSERT_SQL_RELATION_MEMBER_PGSQL =
    	"INSERT INTO relation_members (relation_id, version, member_type, member_id, member_role, sequence_id)"
            + " VALUES (?, ?, ?::nwr_enum, ?, ?, ?)";

    private static final String DELETE_SQL_RELATION_MEMBER =
    	"DELETE FROM relation_members WHERE relation_id = ? AND version = ?";

    private static final String INSERT_SQL_RELATION_MEMBER_CURRENT_MYSQL =
    	"INSERT INTO current_relation_members (relation_id, member_type, member_id, member_role, sequence_id)"
            + " VALUES (?, ?, ?, ?, ?)";

    private static final String INSERT_SQL_RELATION_MEMBER_CURRENT_PGSQL =
    	"INSERT INTO current_relation_members (relation_id, member_type, member_id, member_role, sequence_id)"
            + " VALUES (?, ?::nwr_enum, ?, ?, ?)";

    private static final String DELETE_SQL_RELATION_MEMBER_CURRENT =
    	"DELETE FROM current_relation_members WHERE relation_id = ?";

    private final DatabaseContext dbCtx;
    private final UserManager userManager;
    private final ChangesetManager changesetManager;
    private final boolean populateCurrentTables;
    private final ReleasableStatementContainer statementContainer;
    private PreparedStatement insertNodeStatement;
    private PreparedStatement updateNodeStatement;
    private PreparedStatement selectNodeCountStatement;
    private PreparedStatement insertNodeCurrentStatement;
    private PreparedStatement updateNodeCurrentStatement;
    private PreparedStatement selectNodeCurrentCountStatement;
    private PreparedStatement insertNodeTagStatement;
    private PreparedStatement deleteNodeTagStatement;
    private PreparedStatement insertNodeTagCurrentStatement;
    private PreparedStatement deleteNodeTagCurrentStatement;
    private PreparedStatement insertWayStatement;
    private PreparedStatement updateWayStatement;
    private PreparedStatement selectWayCountStatement;
    private PreparedStatement insertWayCurrentStatement;
    private PreparedStatement updateWayCurrentStatement;
    private PreparedStatement selectWayCurrentCountStatement;
    private PreparedStatement insertWayTagStatement;
    private PreparedStatement deleteWayTagStatement;
    private PreparedStatement insertWayTagCurrentStatement;
    private PreparedStatement deleteWayTagCurrentStatement;
    private PreparedStatement insertWayNodeStatement;
    private PreparedStatement deleteWayNodeStatement;
    private PreparedStatement insertWayNodeCurrentStatement;
    private PreparedStatement deleteWayNodeCurrentStatement;
    private PreparedStatement insertRelationStatement;
    private PreparedStatement updateRelationStatement;
    private PreparedStatement selectRelationCountStatement;
    private PreparedStatement insertRelationCurrentStatement;
    private PreparedStatement updateRelationCurrentStatement;
    private PreparedStatement selectRelationCurrentCountStatement;
    private PreparedStatement insertRelationTagStatement;
    private PreparedStatement deleteRelationTagStatement;
    private PreparedStatement insertRelationTagCurrentStatement;
    private PreparedStatement deleteRelationTagCurrentStatement;
    private PreparedStatement insertRelationMemberStatement;
    private PreparedStatement deleteRelationMemberStatement;
    private PreparedStatement insertRelationMemberCurrentStatement;
    private PreparedStatement deleteRelationMemberCurrentStatement;
    private final MemberTypeRenderer memberTypeRenderer;
    private final TileCalculator tileCalculator;

    /**
     * Creates a new instance.
     * 
     * @param loginCredentials Contains all information required to connect to the database.
     * @param populateCurrentTables If true, the current tables will be populated as well as history
     *        tables.
     */
    public ChangeWriter(DatabaseLoginCredentials loginCredentials, boolean populateCurrentTables) {
        dbCtx = new DatabaseContext(loginCredentials);

        statementContainer = new ReleasableStatementContainer();
        userManager = new UserManager(dbCtx);
        changesetManager = new ChangesetManager(dbCtx);

        this.populateCurrentTables = populateCurrentTables;

        tileCalculator = new TileCalculator();
        memberTypeRenderer = new MemberTypeRenderer();
    }

    /**
     * Checks to see if the specified entity exists.
     * 
     * @param statement The statement used to perform the check.
     * @param id The entity identifier.
     * @return True if the entity exists, false otherwise.
     * @throws SQLException if an error occurs accessing the database.
     */
    private boolean checkIfEntityExists(PreparedStatement statement, long id) throws SQLException {
        boolean exists;
        ResultSet resultSet;

        resultSet = null;
        try {
            statement.setLong(1, id);

            resultSet = statement.executeQuery();

            resultSet.next();

            if (resultSet.getInt("rowCount") == 0) {
                exists = false;
            } else {
                exists = true;
            }

            resultSet.close();
            resultSet = null;

            return exists;

        } finally {
            if (resultSet != null) {
                try {
                    resultSet.close();
                } catch (SQLException e) {
                    // We are already in an error condition so log and continue.
                    LOG.log(Level.WARNING, "Unable to close entity existence check result set.", e);
                }
            }
        }
    }

    /**
     * Checks to see if the specified entity history item exists.
     * 
     * @param statement The statement used to perform the check.
     * @param id The entity identifier.
     * @param version The entity version.
     * @return True if the entity exists, false otherwise.
     * @throws SQLException if an error occurs accessing the database.
     */
    private boolean checkIfEntityHistoryExists(PreparedStatement statement, long id, int version) throws SQLException {
        boolean exists;
        ResultSet resultSet;
        int prmIndex;

        resultSet = null;
        try {
            prmIndex = 1;
            statement.setLong(prmIndex++, id);
            statement.setInt(prmIndex++, version);

            resultSet = statement.executeQuery();

            resultSet.next();

            if (resultSet.getInt("rowCount") == 0) {
                exists = false;
            } else {
                exists = true;
            }

            resultSet.close();
            resultSet = null;

            return exists;

        } finally {
            if (resultSet != null) {
                try {
                    resultSet.close();
                } catch (SQLException e) {
                    // We are already in an error condition so log and continue.
                    LOG.log(Level.WARNING, "Unable to close entity history existence result set.", e);
                }
            }
        }
    }

    /**
     * Writes the specified node change to the database.
     * 
     * @param node The node to be written.
     * @param action The change to be applied.
     */
    public void write(Node node, ChangeAction action) {
        boolean visible;
        boolean exists;
        int prmIndex;

        // We can't write an entity with a null timestamp.
        if (node.getTimestamp() == null) {
            throw new OsmosisRuntimeException("Node " + node.getId() + " does not have a timestamp set.");
        }

        // Add or update the user in the database.
        userManager.addOrUpdateUser(node.getUser());
        
        // Create the changeset in the database.
        changesetManager.addChangesetIfRequired(node.getChangesetId(), node.getUser());

        // If this is a deletion, the entity is not visible.
        visible = !action.equals(ChangeAction.Delete);

        // Create the prepared statements for node creation if necessary.
        if (insertNodeStatement == null) {
            insertNodeStatement = statementContainer.add(dbCtx.prepareStatement(INSERT_SQL_NODE));
            updateNodeStatement = statementContainer.add(dbCtx.prepareStatement(UPDATE_SQL_NODE));
            selectNodeCountStatement = statementContainer.add(dbCtx.prepareStatement(SELECT_SQL_NODE_COUNT));
            insertNodeCurrentStatement = statementContainer.add(dbCtx.prepareStatement(INSERT_SQL_NODE_CURRENT));
            updateNodeCurrentStatement = statementContainer.add(dbCtx.prepareStatement(UPDATE_SQL_NODE_CURRENT));
            selectNodeCurrentCountStatement = statementContainer.add(dbCtx
                    .prepareStatement(SELECT_SQL_NODE_CURRENT_COUNT));
            insertNodeTagStatement = statementContainer.add(dbCtx.prepareStatement(INSERT_SQL_NODE_TAG));
            deleteNodeTagStatement = statementContainer.add(dbCtx.prepareStatement(DELETE_SQL_NODE_TAG));
            insertNodeTagCurrentStatement = statementContainer.add(dbCtx.prepareStatement(INSERT_SQL_NODE_TAG_CURRENT));
            deleteNodeTagCurrentStatement = statementContainer.add(dbCtx.prepareStatement(DELETE_SQL_NODE_TAG_CURRENT));
        }

        // Remove the existing tags of the node history item.
        try {
            prmIndex = 1;
            deleteNodeTagStatement.setLong(prmIndex++, node.getId());
            deleteNodeTagStatement.setInt(prmIndex++, node.getVersion());

            deleteNodeTagStatement.execute();

        } catch (SQLException e) {
            throw new OsmosisRuntimeException("Unable to delete node history tags for node with id=" + node.getId()
                    + ".", e);
        }

        // Update the node if it already exists in the history table, otherwise insert it.
        try {
            exists = checkIfEntityHistoryExists(selectNodeCountStatement, node.getId(), node.getVersion());

        } catch (SQLException e) {
            throw new OsmosisRuntimeException(
            		"Unable to check if current node with id=" + node.getId() + " exists.", e);
        }
        if (exists) {
            // Update the node in the history table.
            try {
                prmIndex = 1;
                updateNodeStatement.setTimestamp(prmIndex++, new Timestamp(node.getTimestamp().getTime()));
                updateNodeStatement.setBoolean(prmIndex++, visible);
                updateNodeStatement.setLong(prmIndex++, node.getChangesetId());
                updateNodeStatement.setInt(prmIndex++, FixedPrecisionCoordinateConvertor.convertToFixed(node
                        .getLatitude()));
                updateNodeStatement.setInt(prmIndex++, FixedPrecisionCoordinateConvertor.convertToFixed(node
                        .getLongitude()));
                updateNodeStatement.setLong(prmIndex++, tileCalculator.calculateTile(node.getLatitude(), node
                        .getLongitude()));
                updateNodeStatement.setLong(prmIndex++, node.getId());
                updateNodeStatement.setInt(prmIndex++, node.getVersion());

                updateNodeStatement.execute();

            } catch (SQLException e) {
                throw new OsmosisRuntimeException("Unable to update history node with id=" + node.getId() + ".", e);
            }
        } else {
            // Insert the new node into the history table.
            try {
                prmIndex = 1;
                insertNodeStatement.setLong(prmIndex++, node.getId());
                insertNodeStatement.setInt(prmIndex++, node.getVersion());
                insertNodeStatement.setTimestamp(prmIndex++, new Timestamp(node.getTimestamp().getTime()));
                insertNodeStatement.setBoolean(prmIndex++, visible);
                insertNodeStatement.setLong(prmIndex++, node.getChangesetId());
                insertNodeStatement.setInt(prmIndex++, FixedPrecisionCoordinateConvertor.convertToFixed(node
                        .getLatitude()));
                insertNodeStatement.setInt(prmIndex++, FixedPrecisionCoordinateConvertor.convertToFixed(node
                        .getLongitude()));
                insertNodeStatement.setLong(prmIndex++, tileCalculator.calculateTile(node.getLatitude(), node
                        .getLongitude()));

                insertNodeStatement.execute();

            } catch (SQLException e) {
                throw new OsmosisRuntimeException("Unable to insert history node with id=" + node.getId() + ".", e);
            }
        }

        // Insert the tags of the new node into the history table.
        for (Tag tag : node.getTags()) {
            try {
                prmIndex = 1;
                insertNodeTagStatement.setLong(prmIndex++, node.getId());
                insertNodeTagStatement.setInt(prmIndex++, node.getVersion());
                insertNodeTagStatement.setString(prmIndex++, tag.getKey());
                insertNodeTagStatement.setString(prmIndex++, tag.getValue());

                insertNodeTagStatement.execute();

            } catch (SQLException e) {
                throw new OsmosisRuntimeException("Unable to insert history node tag with id=" + node.getId()
                        + " and key=(" + tag.getKey() + ").", e);
            }
        }

        if (populateCurrentTables) {
            // Delete the existing node tags from the current table.
            try {
                deleteNodeTagCurrentStatement.setLong(1, node.getId());

                deleteNodeTagCurrentStatement.execute();

            } catch (SQLException e) {
                throw new OsmosisRuntimeException(
                		"Unable to delete current node tags with id=" + node.getId() + ".", e);
            }

            // Update the node if it already exists in the current table, otherwise insert it.
            try {
                exists = checkIfEntityExists(selectNodeCurrentCountStatement, node.getId());

            } catch (SQLException e) {
                throw new OsmosisRuntimeException("Unable to check if current node with id=" + node.getId()
                        + " exists.", e);
            }
            if (exists) {
                // Update the node in the current table.
                try {
                    prmIndex = 1;
                    updateNodeCurrentStatement.setInt(prmIndex++, node.getVersion());
                    updateNodeCurrentStatement.setTimestamp(prmIndex++, new Timestamp(node.getTimestamp().getTime()));
                    updateNodeCurrentStatement.setBoolean(prmIndex++, visible);
                    updateNodeCurrentStatement.setLong(prmIndex++, node.getChangesetId());
                    updateNodeCurrentStatement.setInt(prmIndex++, FixedPrecisionCoordinateConvertor.convertToFixed(node
                            .getLatitude()));
                    updateNodeCurrentStatement.setInt(prmIndex++, FixedPrecisionCoordinateConvertor.convertToFixed(node
                            .getLongitude()));
                    updateNodeCurrentStatement.setLong(prmIndex++, tileCalculator.calculateTile(node.getLatitude(),
                            node.getLongitude()));
                    updateNodeCurrentStatement.setLong(prmIndex++, node.getId());

                    updateNodeCurrentStatement.execute();

                } catch (SQLException e) {
                    throw new OsmosisRuntimeException("Unable to update current node with id=" + node.getId() + ".", e);
                }
            } else {
                // Insert the new node into the current table.
                try {
                    prmIndex = 1;
                    insertNodeCurrentStatement.setLong(prmIndex++, node.getId());
                    insertNodeCurrentStatement.setInt(prmIndex++, node.getVersion());
                    insertNodeCurrentStatement.setTimestamp(prmIndex++, new Timestamp(node.getTimestamp().getTime()));
                    insertNodeCurrentStatement.setBoolean(prmIndex++, visible);
                    insertNodeCurrentStatement.setLong(prmIndex++, node.getChangesetId());
                    insertNodeCurrentStatement.setInt(prmIndex++, FixedPrecisionCoordinateConvertor.convertToFixed(node
                            .getLatitude()));
                    insertNodeCurrentStatement.setInt(prmIndex++, FixedPrecisionCoordinateConvertor.convertToFixed(node
                            .getLongitude()));
                    insertNodeCurrentStatement.setLong(prmIndex++, tileCalculator.calculateTile(node.getLatitude(),
                            node.getLongitude()));

                    insertNodeCurrentStatement.execute();

                } catch (SQLException e) {
                    throw new OsmosisRuntimeException("Unable to insert current node with id=" + node.getId() + ".", e);
                }
            }

            // Insert the tags of the new node into the current table.
            for (Tag tag : node.getTags()) {
                try {
                    prmIndex = 1;
                    insertNodeTagCurrentStatement.setLong(prmIndex++, node.getId());
                    insertNodeTagCurrentStatement.setString(prmIndex++, tag.getKey());
                    insertNodeTagCurrentStatement.setString(prmIndex++, tag.getValue());

                    insertNodeTagCurrentStatement.execute();

                } catch (SQLException e) {
                    throw new OsmosisRuntimeException("Unable to insert current node tag with id=" + node.getId()
                            + " and key=(" + tag.getKey() + ").", e);
                }
            }
        }
    }

    /**
     * Writes the specified way change to the database.
     * 
     * @param way The way to be written.
     * @param action The change to be applied.
     */
    public void write(Way way, ChangeAction action) {
        boolean visible;
        boolean exists;
        int prmIndex;
        List<WayNode> nodeReferenceList;

        // We can't write an entity with a null timestamp.
        if (way.getTimestamp() == null) {
            throw new OsmosisRuntimeException("Way " + way.getId() + " does not have a timestamp set.");
        }

        // Add or update the user in the database.
        userManager.addOrUpdateUser(way.getUser());
        
        // Create the changeset in the database.
        changesetManager.addChangesetIfRequired(way.getChangesetId(), way.getUser());

        nodeReferenceList = way.getWayNodes();

        // If this is a deletion, the entity is not visible.
        visible = !action.equals(ChangeAction.Delete);

        // Create the prepared statements for way creation if necessary.
        if (insertWayStatement == null) {
            insertWayStatement = statementContainer.add(dbCtx.prepareStatement(INSERT_SQL_WAY));
            updateWayStatement = statementContainer.add(dbCtx.prepareStatement(UPDATE_SQL_WAY));
            selectWayCountStatement = statementContainer.add(dbCtx.prepareStatement(SELECT_SQL_WAY_COUNT));
            insertWayCurrentStatement = statementContainer.add(dbCtx.prepareStatement(INSERT_SQL_WAY_CURRENT));
            updateWayCurrentStatement = statementContainer.add(dbCtx.prepareStatement(UPDATE_SQL_WAY_CURRENT));
            selectWayCurrentCountStatement = statementContainer.add(dbCtx
                    .prepareStatement(SELECT_SQL_WAY_CURRENT_COUNT));
            insertWayTagStatement = statementContainer.add(dbCtx.prepareStatement(INSERT_SQL_WAY_TAG));
            deleteWayTagStatement = statementContainer.add(dbCtx.prepareStatement(DELETE_SQL_WAY_TAG));
            insertWayTagCurrentStatement = statementContainer.add(dbCtx.prepareStatement(INSERT_SQL_WAY_TAG_CURRENT));
            deleteWayTagCurrentStatement = statementContainer.add(dbCtx.prepareStatement(DELETE_SQL_WAY_TAG_CURRENT));
            insertWayNodeStatement = statementContainer.add(dbCtx.prepareStatement(INSERT_SQL_WAY_NODE));
            deleteWayNodeStatement = statementContainer.add(dbCtx.prepareStatement(DELETE_SQL_WAY_NODE));
            insertWayNodeCurrentStatement = statementContainer.add(dbCtx.prepareStatement(INSERT_SQL_WAY_NODE_CURRENT));
            deleteWayNodeCurrentStatement = statementContainer.add(dbCtx.prepareStatement(DELETE_SQL_WAY_NODE_CURRENT));
        }

        // Remove the existing tags of the way history item.
        try {
            prmIndex = 1;
            deleteWayTagStatement.setLong(prmIndex++, way.getId());
            deleteWayTagStatement.setInt(prmIndex++, way.getVersion());

            deleteWayTagStatement.execute();

        } catch (SQLException e) {
            throw new OsmosisRuntimeException("Unable to delete way history tags for way with id=" + way.getId() + ".",
                    e);
        }

        // Remove the existing way nodes of the way history item.
        try {
            prmIndex = 1;
            deleteWayNodeStatement.setLong(prmIndex++, way.getId());
            deleteWayNodeStatement.setInt(prmIndex++, way.getVersion());

            deleteWayNodeStatement.execute();

        } catch (SQLException e) {
            throw new OsmosisRuntimeException(
                    "Unable to delete way history nodes for way with id=" + way.getId() + ".", e);
        }

        // Update the way if it already exists in the history table, otherwise insert it.
        try {
            exists = checkIfEntityHistoryExists(selectWayCountStatement, way.getId(), way.getVersion());

        } catch (SQLException e) {
            throw new OsmosisRuntimeException("Unable to check if current way with id=" + way.getId() + " exists.", e);
        }
        if (exists) {
            // Update the way in the history table.
            try {
                prmIndex = 1;
                updateWayStatement.setTimestamp(prmIndex++, new Timestamp(way.getTimestamp().getTime()));
                updateWayStatement.setBoolean(prmIndex++, visible);
                updateWayStatement.setLong(prmIndex++, way.getChangesetId());
                updateWayStatement.setLong(prmIndex++, way.getId());
                updateWayStatement.setInt(prmIndex++, way.getVersion());

                updateWayStatement.execute();

            } catch (SQLException e) {
                throw new OsmosisRuntimeException("Unable to update history way with id=" + way.getId() + ".", e);
            }
        } else {
            // Insert the new way into the history table.
            try {
                prmIndex = 1;
                insertWayStatement.setLong(prmIndex++, way.getId());
                insertWayStatement.setInt(prmIndex++, way.getVersion());
                insertWayStatement.setTimestamp(prmIndex++, new Timestamp(way.getTimestamp().getTime()));
                insertWayStatement.setBoolean(prmIndex++, visible);
                insertWayStatement.setLong(prmIndex++, way.getChangesetId());

                insertWayStatement.execute();

            } catch (SQLException e) {
                throw new OsmosisRuntimeException("Unable to insert history way with id=" + way.getId() + ".", e);
            }
        }

        // Insert the tags of the new way into the history table.
        for (Tag tag : way.getTags()) {
            try {
                prmIndex = 1;
                insertWayTagStatement.setLong(prmIndex++, way.getId());
                insertWayTagStatement.setInt(prmIndex++, way.getVersion());
                insertWayTagStatement.setString(prmIndex++, tag.getKey());
                insertWayTagStatement.setString(prmIndex++, tag.getValue());

                insertWayTagStatement.execute();

            } catch (SQLException e) {
                throw new OsmosisRuntimeException("Unable to insert history way tag with id=" + way.getId()
                        + " and key=(" + tag.getKey() + ").", e);
            }
        }

        // Insert the nodes of the new way into the history table.
        for (int i = 0; i < nodeReferenceList.size(); i++) {
            WayNode nodeReference;

            nodeReference = nodeReferenceList.get(i);

            try {
                prmIndex = 1;
                insertWayNodeStatement.setLong(prmIndex++, way.getId());
                insertWayNodeStatement.setInt(prmIndex++, way.getVersion());
                insertWayNodeStatement.setLong(prmIndex++, nodeReference.getNodeId());
                insertWayNodeStatement.setLong(prmIndex++, i + 1);

                insertWayNodeStatement.execute();

            } catch (SQLException e) {
                throw new OsmosisRuntimeException("Unable to insert history way node with way id=" + way.getId()
                        + " and node id=" + nodeReference.getNodeId() + ".", e);
            }
        }

        if (populateCurrentTables) {
            // Delete the existing way tags from the current table.
            try {
                deleteWayTagCurrentStatement.setLong(1, way.getId());

                deleteWayTagCurrentStatement.execute();

            } catch (SQLException e) {
                throw new OsmosisRuntimeException("Unable to delete current way tags with id=" + way.getId() + ".", e);
            }
            // Delete the existing way nodes from the current table.
            try {
                deleteWayNodeCurrentStatement.setLong(1, way.getId());

                deleteWayNodeCurrentStatement.execute();

            } catch (SQLException e) {
                throw new OsmosisRuntimeException("Unable to delete current way nodes with id=" + way.getId() + ".", e);
            }

            // Update the node if it already exists in the current table, otherwise insert it.
            try {
                exists = checkIfEntityExists(selectWayCurrentCountStatement, way.getId());

            } catch (SQLException e) {
                throw new OsmosisRuntimeException("Unable to check if current way with id=" + way.getId() + " exists.",
                        e);
            }
            if (exists) {
                // Update the way in the current table.
                try {
                    prmIndex = 1;
                    updateWayCurrentStatement.setInt(prmIndex++, way.getVersion());
                    updateWayCurrentStatement.setTimestamp(prmIndex++, new Timestamp(way.getTimestamp().getTime()));
                    updateWayCurrentStatement.setBoolean(prmIndex++, visible);
                    updateWayCurrentStatement.setLong(prmIndex++, way.getChangesetId());
                    updateWayCurrentStatement.setLong(prmIndex++, way.getId());

                    updateWayCurrentStatement.execute();

                } catch (SQLException e) {
                    throw new OsmosisRuntimeException("Unable to update current way with id=" + way.getId() + ".", e);
                }
            } else {
                // Insert the new way into the current table.
                try {
                    prmIndex = 1;
                    insertWayCurrentStatement.setLong(prmIndex++, way.getId());
                    insertWayCurrentStatement.setInt(prmIndex++, way.getVersion());
                    insertWayCurrentStatement.setTimestamp(prmIndex++, new Timestamp(way.getTimestamp().getTime()));
                    insertWayCurrentStatement.setBoolean(prmIndex++, visible);
                    insertWayCurrentStatement.setLong(prmIndex++, way.getChangesetId());

                    insertWayCurrentStatement.execute();

                } catch (SQLException e) {
                    throw new OsmosisRuntimeException("Unable to insert current way with id=" + way.getId() + ".", e);
                }
            }

            // Insert the tags of the new way into the current table.
            for (Tag tag : way.getTags()) {
                try {
                    prmIndex = 1;
                    insertWayTagCurrentStatement.setLong(prmIndex++, way.getId());
                    insertWayTagCurrentStatement.setString(prmIndex++, tag.getKey());
                    insertWayTagCurrentStatement.setString(prmIndex++, tag.getValue());

                    insertWayTagCurrentStatement.execute();

                } catch (SQLException e) {
                    throw new OsmosisRuntimeException("Unable to insert current way tag with id=" + way.getId()
                            + " and key=(" + tag.getKey() + ").", e);
                }
            }

            // Insert the nodes of the new way into the current table.
            for (int i = 0; i < nodeReferenceList.size(); i++) {
                WayNode nodeReference;

                nodeReference = nodeReferenceList.get(i);

                try {
                    prmIndex = 1;
                    insertWayNodeCurrentStatement.setLong(prmIndex++, way.getId());
                    insertWayNodeCurrentStatement.setLong(prmIndex++, nodeReference.getNodeId());
                    insertWayNodeCurrentStatement.setLong(prmIndex++, i);

                    insertWayNodeCurrentStatement.execute();

                } catch (SQLException e) {
                    throw new OsmosisRuntimeException("Unable to insert current way node with way id=" + way.getId()
                            + " and node id=" + nodeReference.getNodeId() + ".", e);
                }
            }
        }
    }

    /**
     * Writes the specified relation change to the database.
     * 
     * @param relation The relation to be written.
     * @param action The change to be applied.
     */
    public void write(Relation relation, ChangeAction action) {
        boolean visible;
        boolean exists;
        int prmIndex;
        List<RelationMember> relationMemberList;

        // We can't write an entity with a null timestamp.
        if (relation.getTimestamp() == null) {
            throw new OsmosisRuntimeException("Relation " + relation.getId() + " does not have a timestamp set.");
        }

        // Add or update the user in the database.
        userManager.addOrUpdateUser(relation.getUser());
        
        // Create the changeset in the database.
        changesetManager.addChangesetIfRequired(relation.getChangesetId(), relation.getUser());

        relationMemberList = relation.getMembers();

        // If this is a deletion, the entity is not visible.
        visible = !action.equals(ChangeAction.Delete);

        // Create the prepared statements for relation creation if necessary.
        if (insertRelationStatement == null) {
            insertRelationStatement = statementContainer.add(dbCtx.prepareStatement(INSERT_SQL_RELATION));
            updateRelationStatement = statementContainer.add(dbCtx.prepareStatement(UPDATE_SQL_RELATION));
            selectRelationCountStatement = statementContainer.add(dbCtx.prepareStatement(SELECT_SQL_RELATION_COUNT));
            insertRelationCurrentStatement = statementContainer
                    .add(dbCtx.prepareStatement(INSERT_SQL_RELATION_CURRENT));
            updateRelationCurrentStatement = statementContainer
                    .add(dbCtx.prepareStatement(UPDATE_SQL_RELATION_CURRENT));
            selectRelationCurrentCountStatement = statementContainer.add(dbCtx
                    .prepareStatement(SELECT_SQL_RELATION_CURRENT_COUNT));
            insertRelationTagStatement = statementContainer.add(dbCtx.prepareStatement(INSERT_SQL_RELATION_TAG));
            deleteRelationTagStatement = statementContainer.add(dbCtx.prepareStatement(DELETE_SQL_RELATION_TAG));
            insertRelationTagCurrentStatement = statementContainer.add(dbCtx
                    .prepareStatement(INSERT_SQL_RELATION_TAG_CURRENT));
            deleteRelationTagCurrentStatement = statementContainer.add(dbCtx
                    .prepareStatement(DELETE_SQL_RELATION_TAG_CURRENT));
            switch (dbCtx.getDatabaseType()) {
            case POSTGRESQL:
            	insertRelationMemberStatement =
            		statementContainer.add(dbCtx.prepareStatement(INSERT_SQL_RELATION_MEMBER_PGSQL));
                break;
            case MYSQL:
            	insertRelationMemberStatement =
            		statementContainer.add(dbCtx.prepareStatement(INSERT_SQL_RELATION_MEMBER_MYSQL));
                break;
            default:
                throw new OsmosisRuntimeException("Unknown database type " + dbCtx.getDatabaseType() + ".");
            }
            deleteRelationMemberStatement = statementContainer.add(dbCtx.prepareStatement(DELETE_SQL_RELATION_MEMBER));
            switch (dbCtx.getDatabaseType()) {
            case POSTGRESQL:
            	insertRelationMemberCurrentStatement =
            		statementContainer.add(dbCtx.prepareStatement(INSERT_SQL_RELATION_MEMBER_CURRENT_PGSQL));
                break;
            case MYSQL:
            	insertRelationMemberCurrentStatement =
            		statementContainer.add(dbCtx.prepareStatement(INSERT_SQL_RELATION_MEMBER_CURRENT_MYSQL));
                break;
            default:
                throw new OsmosisRuntimeException("Unknown database type " + dbCtx.getDatabaseType() + ".");
            }
            deleteRelationMemberCurrentStatement = statementContainer.add(dbCtx
                    .prepareStatement(DELETE_SQL_RELATION_MEMBER_CURRENT));
        }

        // Remove the existing tags of the relation history item.
        try {
            prmIndex = 1;
            deleteRelationTagStatement.setLong(prmIndex++, relation.getId());
            deleteRelationTagStatement.setInt(prmIndex++, relation.getVersion());

            deleteRelationTagStatement.execute();

        } catch (SQLException e) {
            throw new OsmosisRuntimeException("Unable to delete relation history tags for relation with id="
                    + relation.getId() + ".", e);
        }

        // Remove the existing relation members of the relation history item.
        try {
            prmIndex = 1;
            deleteRelationMemberStatement.setLong(prmIndex++, relation.getId());
            deleteRelationMemberStatement.setInt(prmIndex++, relation.getVersion());

            deleteRelationMemberStatement.execute();

        } catch (SQLException e) {
            throw new OsmosisRuntimeException("Unable to delete relation history members for relation with id="
                    + relation.getId() + ".", e);
        }

        // Update the relation if it already exists in the history table, otherwise insert it.
        try {
            exists = checkIfEntityHistoryExists(selectRelationCountStatement, relation.getId(), relation.getVersion());

        } catch (SQLException e) {
            throw new OsmosisRuntimeException("Unable to check if current relation with id=" + relation.getId()
                    + " exists.", e);
        }
        if (exists) {
            // Update the relation in the history table.
            try {
                prmIndex = 1;
                updateRelationStatement.setTimestamp(prmIndex++, new Timestamp(relation.getTimestamp().getTime()));
                updateRelationStatement.setBoolean(prmIndex++, visible);
                updateRelationStatement.setLong(prmIndex++, relation.getChangesetId());
                updateRelationStatement.setLong(prmIndex++, relation.getId());
                updateRelationStatement.setInt(prmIndex++, relation.getVersion());

                updateRelationStatement.execute();

            } catch (SQLException e) {
                throw new OsmosisRuntimeException(
                        "Unable to update history relation with id=" + relation.getId() + ".", e);
            }
        } else {
            // Insert the new relation into the history table.
            try {
                prmIndex = 1;
                insertRelationStatement.setLong(prmIndex++, relation.getId());
                insertRelationStatement.setInt(prmIndex++, relation.getVersion());
                insertRelationStatement.setTimestamp(prmIndex++, new Timestamp(relation.getTimestamp().getTime()));
                insertRelationStatement.setBoolean(prmIndex++, visible);
                insertRelationStatement.setLong(prmIndex++, relation.getChangesetId());

                insertRelationStatement.execute();

            } catch (SQLException e) {
                throw new OsmosisRuntimeException(
                        "Unable to insert history relation with id=" + relation.getId() + ".", e);
            }
        }

        // Insert the tags of the new relation into the history table.
        for (Tag tag : relation.getTags()) {
            try {
                prmIndex = 1;
                insertRelationTagStatement.setLong(prmIndex++, relation.getId());
                insertRelationTagStatement.setInt(prmIndex++, relation.getVersion());
                insertRelationTagStatement.setString(prmIndex++, tag.getKey());
                insertRelationTagStatement.setString(prmIndex++, tag.getValue());

                insertRelationTagStatement.execute();

            } catch (SQLException e) {
                throw new OsmosisRuntimeException("Unable to insert history relation tag with id=" + relation.getId()
                        + " and key=(" + tag.getKey() + ").", e);
            }
        }

        // Insert the members of the new relation into the history table.
        for (int i = 0; i < relationMemberList.size(); i++) {
            RelationMember relationMember;

            relationMember = relationMemberList.get(i);

            try {
                prmIndex = 1;
                insertRelationMemberStatement.setLong(prmIndex++, relation.getId());
                insertRelationMemberStatement.setInt(prmIndex++, relation.getVersion());
                insertRelationMemberStatement.setString(prmIndex++, memberTypeRenderer.render(relationMember
                        .getMemberType()));
                insertRelationMemberStatement.setLong(prmIndex++, relationMember.getMemberId());
                insertRelationMemberStatement.setString(prmIndex++, relationMember.getMemberRole());
                insertRelationMemberStatement.setInt(prmIndex++, i + 1);

                insertRelationMemberStatement.execute();

            } catch (SQLException e) {
                throw new OsmosisRuntimeException("Unable to insert history relation member with relation id="
                        + relation.getId() + ", member type=" + relationMember.getMemberId() + " and member id="
                        + relationMember.getMemberId() + ".", e);
            }
        }

        if (populateCurrentTables) {
            // Delete the existing relation tags from the current table.
            try {
                deleteRelationTagCurrentStatement.setLong(1, relation.getId());

                deleteRelationTagCurrentStatement.execute();

            } catch (SQLException e) {
                throw new OsmosisRuntimeException("Unable to delete current relation tags with id=" + relation.getId()
                        + ".", e);
            }
            // Delete the existing relation members from the current table.
            try {
                deleteRelationMemberCurrentStatement.setLong(1, relation.getId());

                deleteRelationMemberCurrentStatement.execute();

            } catch (SQLException e) {
                throw new OsmosisRuntimeException("Unable to delete current relation members with id="
                        + relation.getId() + ".", e);
            }

            // Update the relation if it already exists in the current table, otherwise insert it.
            try {
                exists = checkIfEntityExists(selectRelationCurrentCountStatement, relation.getId());

            } catch (SQLException e) {
                throw new OsmosisRuntimeException("Unable to check if current relation with id=" + relation.getId()
                        + " exists.", e);
            }
            if (exists) {
                // Update the relation in the current table.
                try {
                    prmIndex = 1;
                    updateRelationCurrentStatement.setInt(prmIndex++, relation.getVersion());
                    updateRelationCurrentStatement.setTimestamp(prmIndex++, new Timestamp(relation.getTimestamp()
                            .getTime()));
                    updateRelationCurrentStatement.setBoolean(prmIndex++, visible);
                    updateRelationCurrentStatement.setLong(prmIndex++, relation.getChangesetId());
                    updateRelationCurrentStatement.setLong(prmIndex++, relation.getId());

                    updateRelationCurrentStatement.execute();

                } catch (SQLException e) {
                    throw new OsmosisRuntimeException("Unable to update current relation with id=" + relation.getId()
                            + ".", e);
                }
            } else {
                // Insert the new node into the current table.
                try {
                    prmIndex = 1;
                    insertRelationCurrentStatement.setLong(prmIndex++, relation.getId());
                    insertRelationCurrentStatement.setInt(prmIndex++, relation.getVersion());
                    insertRelationCurrentStatement.setTimestamp(prmIndex++, new Timestamp(relation.getTimestamp()
                            .getTime()));
                    insertRelationCurrentStatement.setBoolean(prmIndex++, visible);
                    insertRelationCurrentStatement.setLong(prmIndex++, relation.getChangesetId());

                    insertRelationCurrentStatement.execute();

                } catch (SQLException e) {
                    throw new OsmosisRuntimeException("Unable to insert current relation with id=" + relation.getId()
                            + ".", e);
                }
            }

            // Insert the tags of the new relation into the current table.
            for (Tag tag : relation.getTags()) {
                try {
                    prmIndex = 1;
                    insertRelationTagCurrentStatement.setLong(prmIndex++, relation.getId());
                    insertRelationTagCurrentStatement.setString(prmIndex++, tag.getKey());
                    insertRelationTagCurrentStatement.setString(prmIndex++, tag.getValue());

                    insertRelationTagCurrentStatement.execute();

                } catch (SQLException e) {
                    throw new OsmosisRuntimeException("Unable to insert current relation tag with id="
                            + relation.getId() + " and key=(" + tag.getKey() + ").", e);
                }
            }

            // Insert the members of the new relation into the current table.
            for (int i = 0; i < relationMemberList.size(); i++) {
                RelationMember relationMember;

                relationMember = relationMemberList.get(i);

                try {
                    prmIndex = 1;
                    insertRelationMemberCurrentStatement.setLong(prmIndex++, relation.getId());
                    insertRelationMemberCurrentStatement.setString(prmIndex++, memberTypeRenderer.render(relationMember
                            .getMemberType()));
                    insertRelationMemberCurrentStatement.setLong(prmIndex++, relationMember.getMemberId());
                    insertRelationMemberCurrentStatement.setString(prmIndex++, relationMember.getMemberRole());
                    insertRelationMemberCurrentStatement.setInt(prmIndex++, i + 1);

                    insertRelationMemberCurrentStatement.execute();

                } catch (SQLException e) {
                    throw new OsmosisRuntimeException("Unable to insert current relation member with relation id="
                            + relation.getId() + ", member type=" + relationMember.getMemberId() + " and member id="
                            + relationMember.getMemberId() + ".", e);
                }
            }
        }
    }

    /**
     * Flushes all changes to the database.
     */
    public void complete() {
        dbCtx.commit();
    }

    /**
     * Releases all database resources.
     */
    public void release() {
        statementContainer.release();
        userManager.release();
        changesetManager.release();

        dbCtx.release();
    }
}
