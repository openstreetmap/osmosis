// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.apidb.v0_6.impl;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.openstreetmap.osmosis.core.OsmosisRuntimeException;
import org.openstreetmap.osmosis.apidb.common.DatabaseContext;
import org.openstreetmap.osmosis.core.database.ReleasableStatementContainer;
import org.openstreetmap.osmosis.core.domain.v0_6.OsmUser;
import org.openstreetmap.osmosis.core.lifecycle.Releasable;

/**
 * Creates or loads the details of the Osmosis user in the database.
 * 
 * @author Brett Henderson
 */
public class UserManager implements Releasable {

    private static final Logger LOG = Logger.getLogger(UserManager.class.getName());

    private static final String SELECT_SQL_USER_EXISTS = "SELECT Count(id) AS userCount FROM users WHERE id = ?";

    private static final String INSERT_SQL_USER = "INSERT INTO users (id, email, pass_crypt,"
            + " creation_time, display_name, data_public, description, home_lat, home_lon, home_zoom,"
            + " nearby, pass_salt) VALUES (?, ?, '00000000000000000000000000000000', NOW(), ?, ?,"
            + " ?, 0, 0, 3, 50, '00000000')";

    private static final String UPDATE_SQL_USER = "UPDATE users SET display_name = ? WHERE id = ?";

    private final DatabaseContext dbCtx;

    private final Set<Integer> updatedUsers;

    private final ReleasableStatementContainer statementContainer;

    private PreparedStatement statementInsert;

    private PreparedStatement statementExists;

    private PreparedStatement statementUpdate;

    /**
     * Creates a new instance.
     * 
     * @param dbCtx The database context to use for all database access.
     */
    public UserManager(DatabaseContext dbCtx) {
        this.dbCtx = dbCtx;

        updatedUsers = new HashSet<Integer>();
        statementContainer = new ReleasableStatementContainer();
    }

    /**
     * Checks if the specified user exists in the database.
     * 
     * @param user The user to check for.
     * @return True if the user exists, false otherwise.
     */
    private boolean doesUserExistInDb(OsmUser user) {
        int prmIndex;
        ResultSet resultSet;

        if (statementExists == null) {
            statementExists = statementContainer.add(dbCtx.prepareStatementForStreaming(SELECT_SQL_USER_EXISTS));
        }

        resultSet = null;
        try {
            boolean result;

            prmIndex = 1;
            statementExists.setInt(prmIndex++, user.getId());

            resultSet = statementExists.executeQuery();
            resultSet.next();

            if (resultSet.getInt("userCount") == 0) {
                result = false;
            } else {
                result = true;
            }

            resultSet.close();
            resultSet = null;

            return result;

        } catch (SQLException e) {
            throw new OsmosisRuntimeException("Unable to check if user with id " + user.getId()
                    + " exists in the database.", e);
        } finally {
            if (resultSet != null) {
                try {
                    resultSet.close();
                } catch (SQLException e) {
                    // We are already in an error condition so log and continue.
                    LOG.log(Level.WARNING, "Unable to close existing user result set.", e);
                }
            }
        }
    }

    /**
     * Inserts the specified user into the database.
     * 
     * @param user The user to be inserted.
     */
    private void insertUser(OsmUser user) {
        int prmIndex;
        String userName;
        boolean dataPublic;

        if (statementInsert == null) {
            statementInsert = statementContainer.add(dbCtx.prepareStatement(INSERT_SQL_USER));
        }

        if (OsmUser.NONE.equals(user)) {
            userName = "Osmosis Anonymous";
            dataPublic = false;
        } else {
            userName = user.getName();
            dataPublic = true;
        }

        try {
            prmIndex = 1;
            statementInsert.setInt(prmIndex++, user.getId());
            statementInsert.setString(prmIndex++, "osmosis_user_" + user.getId() + "@example.com");
            statementInsert.setString(prmIndex++, userName);
            statementInsert.setBoolean(prmIndex++, dataPublic);
            statementInsert.setString(prmIndex++, userName);

            statementInsert.executeUpdate();

        } catch (SQLException e) {
            throw new OsmosisRuntimeException("Unable to insert user with id " + user.getId() + " into the database.",
                    e);
        }
    }

    /**
     * Updates the specified user in the database.
     * 
     * @param user The user to be updated.
     */
    private void updateUser(OsmUser user) {
        int prmIndex;

        if (statementUpdate == null) {
            statementUpdate = statementContainer.add(dbCtx.prepareStatement(UPDATE_SQL_USER));
        }

        try {
            String userName;

            if (OsmUser.NONE.equals(user)) {
                userName = "Osmosis Anonymous";
            } else {
                userName = user.getName();
            }

            prmIndex = 1;
            statementUpdate.setString(prmIndex++, userName);
            statementUpdate.setInt(prmIndex++, user.getId());

            statementUpdate.executeUpdate();

        } catch (SQLException e) {
            throw new OsmosisRuntimeException("Unable to update user with id " + user.getId() + " in the database.", e);
        }
    }

    /**
     * Adds the user to the database or updates the name of the existing database entry if one
     * already exists with the same id.
     * 
     * @param user The user to be created or updated.
     */
    public void addOrUpdateUser(OsmUser user) {
        if (!updatedUsers.contains(user.getId())) {
            if (doesUserExistInDb(user)) {
                updateUser(user);
            } else {
                insertUser(user);
            }

            updatedUsers.add(user.getId());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void release() {
        statementContainer.release();
    }
}
