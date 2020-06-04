// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.database;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Logger;

import javax.sql.DataSource;

/**
 * A check against the database to see if it is locked.
 *
 * @author mcuthbert
 */
public class DatabaseLocker implements AutoCloseable {

    private static Logger logger = Logger.getLogger(DatabaseLocker.class.getSimpleName());
    private final DataSource source;
    private final boolean writeLock;
    private boolean enabled = true;
    private int lockedIdentifier = -1;

    /**
     * Static function to fully unlock the database.
     *
     * @param source {@link DataSource} to execute the query
     */
    public static void fullUnlockDatabase(final DataSource source) {
        unlockDatabase(-1, source);
    }

    private static void unlockDatabase(final int identifier, final DataSource source) {
        try (Connection connection = source.getConnection();
                PreparedStatement statement = connection.prepareStatement("SELECT unlock_database(?)")) {
            statement.setInt(1, identifier);
            final ResultSet result = statement.executeQuery();
            if (result.next()) {
                final boolean unlocked = result.getBoolean(1);
                if (unlocked) {
                    logger.info(String.format("Unlocked database using identifier %d.", identifier));
                    return;
                }
            }
            throw new RuntimeException("Failed to unlock the database");
        } catch (final SQLException e) {
            throw new RuntimeException("Failed to unlock the database", e);
        }
    }

    /**
     * Default Constructor.
     *
     * @param source The DataSource for the connection
     * @param writeLock Whether the lock that is being requested is a write lock
     */
    public DatabaseLocker(final DataSource source, final boolean writeLock) {
        this.source = source;
        this.writeLock = writeLock;
        // check to see if the function exists in the database and if it doesn't then throw a
        // warning on the log and don't try any locking
        try (Connection connection = source.getConnection();
                Statement statement = connection.createStatement()) {
            statement.executeQuery("SELECT 'lock_database'::regproc, 'unlock_database'::regproc");
        } catch (final Exception e) {
            logger.warning("Locking functions do not exist in database. Disabling locking.");
            this.enabled = false;
        }
    }

    /**
     * Helper function for primary lock database function that simply sets the source to empty string.
     *
     * @param process The process that is locking the database
     */
    public void lockDatabase(final String process) {
        this.lockDatabase(process, "");
    }

    /**
     * Will attempt to lock the database.
     *
     * @param process
     *          The process that will lock the database. This would be something like "Extracts" or "Replication".
     * @param description
     *          The source of the process. This is basically a description for the process, like
     *          "Pipeline Extraction for Build X".
     */
    public void lockDatabase(final String process, final String description) {
        if (this.enabled) {
            String lockName = "read";
            if (this.writeLock) {
                lockName = "write";
            }
            try (Connection connection = source.getConnection();
                    PreparedStatement statement = connection.prepareStatement("SELECT lock_database(?, ?, ?, ?)")) {
                statement.setString(1, process);
                statement.setString(2, description);
                statement.setString(3, InetAddress.getLocalHost().getHostName());
                statement.setBoolean(4, this.writeLock);
                final ResultSet result = statement.executeQuery();
                if (result.next()) {
                    this.lockedIdentifier = result.getInt(1);
                    logger.info(String.format("Obtained %s lock to database for process: %s "
                                    + "from source: '%s', with lockedID: %d",
                            lockName, process, description, this.lockedIdentifier));
                } else {
                    throw new RuntimeException("Failed to retrieve lock for the database.");
                }
            } catch (final SQLException | UnknownHostException e) {
                throw new RuntimeException("Failed to lock the database.", e);
            }
        }
    }

    /**
     * Will attempt to unlock the database with the given locked identifier.
     */
    public void unlockDatabase() {
        if (this.enabled && this.lockedIdentifier > 0) {
            unlockDatabase(this.lockedIdentifier, this.source);
        }
    }

    /**
     * Does a full unlock of the database. So no matter what is locking it, this will unlock it.
     */
    public void fullUnlockDatabase() {
        if (this.enabled) {
            fullUnlockDatabase(this.source);
        }
    }

    @Override
    public void close() throws Exception {
        this.unlockDatabase();
    }
}
