// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.database;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreatorFactory;
import org.springframework.jdbc.core.SqlRowSetResultSetExtractor;
import org.springframework.jdbc.support.rowset.SqlRowSet;

/**
 * A check against the database to see if it is locked.
 *
 * @author mcuthbert
 */
public class DatabaseLocker implements AutoCloseable {

    private static Logger logger = Logger.getLogger(DatabaseLocker.class.getSimpleName());
    private final JdbcTemplate jdbc;
    private boolean enabled = true;
    private int lockedIdentifier = -1;

    /**
     * Static function to fully unlock the database.
     *
     * @param jdbc {@link JdbcTemplate} to execute the query
     */
    public static void fullUnlockDatabase(final JdbcTemplate jdbc) {
        final int[] argumentTypes = new int[] {
                Types.INTEGER
        };
        final SqlRowSet result = jdbc.query(new PreparedStatementCreatorFactory("SELECT unlock_database(?)",
                        argumentTypes).newPreparedStatementCreator(Collections.singletonList(-1)),
                new SqlRowSetResultSetExtractor());
        if (result.next()) {
            final boolean unlocked = result.getBoolean(1);
            if (unlocked) {
                logger.info("Unlocked database.");
                return;
            }
        }
        throw new RuntimeException("Failed to unlock the database");
    }

    /**
     * Default Constructor.
     *
     * @param jdbc The Spring JDBC template object
     */
    public DatabaseLocker(final JdbcTemplate jdbc) {
        this.jdbc = jdbc;
        // check to see if the function exists in the database and if it doesn't then throw a
        // warning on the log and don't try any locking
        try {
            this.jdbc.query("SELECT 'unlock_database'::regproc", new SqlRowSetResultSetExtractor());
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
     * @param source
     *          The source of the process. This is basically a description for the process, like
     *          "Pipeline Extraction for Build X".
     */
    public void lockDatabase(final String process, final String source) {
        if (this.enabled) {
            final int[] argumentTypes = new int[] {
                    Types.VARCHAR, Types.VARCHAR, Types.VARCHAR
            };
            final List<String> params = new ArrayList<>();
            params.add(process);
            params.add(source);
            try {
                params.add(InetAddress.getLocalHost().getHostName());
            } catch (final UnknownHostException e) {
                throw new RuntimeException(e);
            }

            final SqlRowSet result = this.jdbc
                    .query(new PreparedStatementCreatorFactory("SELECT lock_database(?, ?, ?)",
                                    argumentTypes).newPreparedStatementCreator(params),
                            new SqlRowSetResultSetExtractor());
            if (result.next()) {
                this.lockedIdentifier = result.getInt(1);
                logger.info(String.format("Locking database for process: %s from source: '%s', with lockedID: %d",
                        process, source, this.lockedIdentifier));
            } else {
                throw new RuntimeException("Failed to lock the database.");
            }
        }
    }

    /**
     * Will attempt to unlock the database with the given locked identifier.
     */
    public void unlockDatabase() {
        if (this.enabled) {
            if (this.lockedIdentifier > 0) {
                final int[] argumentTypes = new int[] {
                        Types.INTEGER
                };
                final List<Integer> params = new ArrayList<>();
                params.add(this.lockedIdentifier);

                final SqlRowSet result = jdbc.query(new PreparedStatementCreatorFactory("SELECT unlock_database(?)",
                        argumentTypes).newPreparedStatementCreator(params), new SqlRowSetResultSetExtractor());
                if (result.next()) {
                    final boolean unlocked = result.getBoolean(1);
                    if (unlocked) {
                        logger.info(String.format("Unlocking database with locked ID: %d", this.lockedIdentifier));
                        return;
                    }
                }
                throw new RuntimeException("Failed to unlock the database.");
            }
        }
    }

    /**
     * Does a full unlock of the database. So no matter what is locking it, this will unlock it.
     */
    public void fullUnlockDatabase() {
        if (this.enabled) {
            fullUnlockDatabase(this.jdbc);
        }
    }

    @Override
    public void close() throws Exception {
        this.unlockDatabase();
    }
}
