// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.apidb.common;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.openstreetmap.osmosis.core.OsmosisRuntimeException;
import org.openstreetmap.osmosis.core.database.DatabaseLoginCredentials;
import org.openstreetmap.osmosis.core.database.DatabaseType;


/**
 * This class manages the lifecycle of JDBC objects to minimise the risk of connection leaks and to
 * support a consistent approach to database access.
 * 
 * @author Brett Henderson
 */
public class DatabaseContext {

    private static final Logger LOG = Logger.getLogger(DatabaseContext.class.getName());

    private final DatabaseLoginCredentials loginCredentials;

    private Connection connection;

	/**
	 * This statement is used in cases where the statement isn't exposed to the client. It is stored
	 * globally here to allow it to remain open after a method return and to simplify resource
	 * cleanup. It will be closed during release or if a new statement is created.
	 */
    private Statement statement;
    
    private IdentityValueLoader identityValueLoader;

    private boolean autoCommit;

    /**
     * Creates a new instance.
     * 
     * @param loginCredentials Contains all information required to connect to the database.
     */
    public DatabaseContext(DatabaseLoginCredentials loginCredentials) {
        this.loginCredentials = loginCredentials;
        autoCommit = false;
        try {
            switch (loginCredentials.getDbType()) {
            case POSTGRESQL:
                Class.forName("org.postgresql.Driver");
                identityValueLoader = new PostgresqlIdentityValueLoader(this);
                break;
            case MYSQL:
                Class.forName("com.mysql.jdbc.Driver");
                identityValueLoader = new MysqlIdentityValueLoader(this);
                break;
            default:
                throw new OsmosisRuntimeException("Unknown database type " + loginCredentials.getDbType() + ".");
            }

        } catch (ClassNotFoundException e) {
            throw new OsmosisRuntimeException("Unable to find database driver.", e);
        }

    }

    /**
     * If no database connection is open, a new connection is opened. The database connection is
     * then returned.
     * 
     * @return The database connection.
     */
    private Connection getConnection() {
        if (connection == null) {
            switch (loginCredentials.getDbType()) {
            case POSTGRESQL:
                connection = getPostgresConnection();
                break;
            case MYSQL:
                connection = getMysqlConnection();
                break;
            default:
                throw new OsmosisRuntimeException("Unknown database type " + loginCredentials.getDbType() + ".");
            }
        }
        
        return connection;
    }

    /**
     * @return postgres connection
     */
    private Connection getPostgresConnection() {
        Connection newConnection = null;
        try {
            LOG.finer("Creating a new database connection.");

            newConnection = DriverManager.getConnection(
            		"jdbc:postgresql://" + loginCredentials.getHost() + "/"
                    + loginCredentials.getDatabase(), // + "?logLevel=2"
                    loginCredentials.getUser(),
                    loginCredentials.getPassword()
            );

            newConnection.setAutoCommit(autoCommit);

        } catch (SQLException e) {
            throw new OsmosisRuntimeException("Unable to establish a database connection.", e);
        }
        return newConnection;
    }

    /**
     * @return The mysql database connection.
     */
    private Connection getMysqlConnection() {
        Connection newConnection = null;
        try {
            String url;

            url = "jdbc:mysql://" + loginCredentials.getHost() + "/" + loginCredentials.getDatabase() + "?user="
                    + loginCredentials.getUser() + "&password=" + loginCredentials.getPassword();

            if (loginCredentials.getForceUtf8()) {
                url += "&useUnicode=true&characterEncoding=UTF-8";
            }
            if (loginCredentials.getProfileSql()) {
                url += "&profileSql=true";
            }

            newConnection = DriverManager.getConnection(url);

            newConnection.setAutoCommit(autoCommit);

        } catch (SQLException e) {
            throw new OsmosisRuntimeException("Unable to establish a database connection.", e);
        }

        return newConnection;
    }


	/**
	 * Returns the database type currently in use. This should only be used when it is not possible
	 * to write database agnostic statements.
	 * 
	 * @return The database type.
	 */
    public DatabaseType getDatabaseType() {
    	return loginCredentials.getDbType();
    }
	
	
    /**
	 * Truncates the contents of the specified tables.
	 * 
	 * @param tables
	 *            The tables to be truncated.
	 */
	public void truncateTables(List<String> tables) {
		switch (loginCredentials.getDbType()) {
        case POSTGRESQL:
        	StringBuilder statementBuilder = new StringBuilder();
    		
			for (String table : tables) {
				if (statementBuilder.length() == 0) {
					statementBuilder.append("TRUNCATE ");
				} else {
					statementBuilder.append(", ");
				}
				
				statementBuilder.append(table);
			}
			
			statementBuilder.append(" CASCADE");
			
			executeStatement(statementBuilder.toString());
			break;
        case MYSQL:
			for (String table : tables) {
				executeStatement("TRUNCATE " + table);
			}
			break;
		default:
			throw new OsmosisRuntimeException("Unknown database type " + loginCredentials.getDbType() + ".");
		}
	}
	
	
    /**
	 * Disables the indexes of the specified tables.
	 * 
	 * @param tables
	 *            The tables to disable indexes on.
	 */
	public void disableIndexes(List<String> tables) {
		switch (loginCredentials.getDbType()) {
        case POSTGRESQL:
			// There is no way to automatically disable all indexes for a table.
			break;
        case MYSQL:
        	for (String table : tables) {
        		executeStatement("ALTER TABLE " + table + " DISABLE KEYS");
			}
			break;
		default:
			throw new OsmosisRuntimeException("Unknown database type " + loginCredentials.getDbType() + ".");
		}
	}
	
	
    /**
	 * Enables the indexes of the specified tables.
	 * 
	 * @param tables
	 *            The tables to enable indexes on.
	 */
	public void enableIndexes(List<String> tables) {
		switch (loginCredentials.getDbType()) {
        case POSTGRESQL:
			// There is no way to automatically disable all indexes for a table.
			break;
        case MYSQL:
        	for (String table : tables) {
        		executeStatement("ALTER TABLE " + table + " ENABLE KEYS");
			}
			break;
		default:
			throw new OsmosisRuntimeException("Unknown database type " + loginCredentials.getDbType() + ".");
		}
	}
	
	
    /**
	 * Locks the specified tables for exclusive access.
	 * 
	 * @param tables
	 *            The tables to lock.
	 */
	public void lockTables(List<String> tables) {
		switch (loginCredentials.getDbType()) {
        case POSTGRESQL:
			// Locking tables is not supported.
			break;
        case MYSQL:
        	StringBuilder statementBuilder = new StringBuilder();
        	
        	for (String table : tables) {
        		if (statementBuilder.length() == 0) {
        			statementBuilder.append("LOCK TABLES ");
        		} else {
        			statementBuilder.append(", ");
        		}
        		statementBuilder.append(table);
        		statementBuilder.append(" WRITE");
			}
        	
        	executeStatement(statementBuilder.toString());
			break;
		default:
			throw new OsmosisRuntimeException("Unknown database type " + loginCredentials.getDbType() + ".");
		}
	}
	
	
    /**
	 * Unlocks the specified tables.
	 * 
	 * @param tables
	 *            The tables to unlock.
	 */
	public void unlockTables(List<String> tables) {
		switch (loginCredentials.getDbType()) {
        case POSTGRESQL:
			// Locking tables is not supported.
			break;
        case MYSQL:
        	executeStatement("UNLOCK TABLES");
			break;
		default:
			throw new OsmosisRuntimeException("Unknown database type " + loginCredentials.getDbType() + ".");
		}
	}


	/**
	 * Gets the last inserted identity column value. This is a global value and may not work
	 * correctly if the database uses triggers.
	 * 
	 * @return The last inserted identity column value.
	 */
	public long getLastInsertId() {
		return identityValueLoader.getLastInsertId();
	}


	/**
	 * Gets the last retrieved sequence value. This is specific to the current connection only.
	 * 
	 * @param sequenceName
	 *            The name of the sequence.
	 * @return The last inserted identity column value.
	 */
	public long getLastSequenceId(String sequenceName) {
		return identityValueLoader.getLastSequenceId(sequenceName);
	}


    /**
     * Executes a sql statement against the database.
     * 
     * @param sql The sql statement to be invoked.
     */
    public void executeStatement(String sql) {
        try {
            if (statement != null) {
                statement.close();
            }

            statement = getConnection().createStatement();

            statement.execute(sql);

        } catch (SQLException e) {
            throw new OsmosisRuntimeException("Unable to execute statement.", e);
        }
    }

    
    /**
	 * Creates a new database prepared statement.
	 * 
	 * @param sql
	 *            The statement to be created.
	 * @return The newly created statement.
	 */
    public PreparedStatement prepareStatement(String sql) {
        try {
            PreparedStatement preparedStatement;

            preparedStatement = getConnection().prepareStatement(sql);

            return preparedStatement;

        } catch (SQLException e) {
            throw new OsmosisRuntimeException("Unable to create database prepared statement.", e);
        }
    }
    
    
    private void setStatementFetchSizeForStreaming(Statement streamingStatement) {
    	try {
	    	switch (loginCredentials.getDbType()) {
	        case POSTGRESQL:
	        	streamingStatement.setFetchSize(10000);
				break;
	        case MYSQL:
	        	streamingStatement.setFetchSize(Integer.MIN_VALUE);
				break;
			default:
				throw new OsmosisRuntimeException("Unknown database type " + loginCredentials.getDbType() + ".");
			}
    	} catch (SQLException e) {
    		throw new OsmosisRuntimeException("Unable to update statement fetch size.", e);
    	}
    }
    

    /**
	 * Creates a new database statement that is configured so that any result sets created using it
	 * will stream data from the database instead of returning all records at once and storing in
	 * memory.
	 * <p>
	 * If no input parameters need to be set on the statement, use the executeStreamingQuery method
	 * instead.
	 * 
	 * @param sql
	 *            The statement to be created. This must be a select statement.
	 * @return The newly created statement.
	 */
    public PreparedStatement prepareStatementForStreaming(String sql) {
        try {
            PreparedStatement newStatement;

            // Create a statement for returning streaming results.
			newStatement = getConnection().prepareStatement(sql, ResultSet.TYPE_FORWARD_ONLY,
					ResultSet.CONCUR_READ_ONLY);

			setStatementFetchSizeForStreaming(newStatement);

            return newStatement;

        } catch (SQLException e) {
            throw new OsmosisRuntimeException("Unable to create streaming resultset statement.", e);
        }
    }

    /**
     * Executes a query and returns a result set. The returned result set must be closed by the
     * caller.
     * 
     * @param sql The query to execute.
     * @return The newly created result set.
     */
    public ResultSet executeQuery(String sql) {
        try {
            ResultSet resultSet;

            LOG.finest("Executing query {" + sql + "}");

            if (statement != null) {
                statement.close();
            }

            statement = getConnection().createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            
            setStatementFetchSizeForStreaming(statement);

            resultSet = statement.executeQuery(sql);

            return resultSet;

        } catch (SQLException e) {
            throw new OsmosisRuntimeException("Unable to create resultset.", e);
        }
    }

    /**
     * Commits any outstanding transaction.
     */
    public void commit() {
        if (connection != null) {
            try {
                connection.commit();
            } catch (SQLException e) {
                throw new OsmosisRuntimeException("Unable to commit changes.", e);
            }
        }
    }

    /**
     * Releases all database resources. This method is guaranteed not to throw transactions and
     * should always be called in a finally block whenever this class is used.
     */
    public void release() {
    	identityValueLoader.release();
    	
        if (statement != null) {
            try {
            	statement.close();

            } catch (SQLException e) {
                // We cannot throw an exception within a release statement.
                LOG.log(Level.WARNING, "Unable to close existing statement.", e);
            }

            statement = null;
        }
        if (connection != null) {
            try {
                connection.close();

            } catch (SQLException e) {
                // We cannot throw an exception within a release statement.
                LOG.log(Level.WARNING, "Unable to close database connection.", e);
            }

            connection = null;
        }
    }

    /**
     * Sets the auto-commit property on the underlying connection.
     * 
     * @param autoCommit The new auto commit value.
     */
    public void setAutoCommit(boolean autoCommit) {
        if (connection != null) {
            try {
                LOG.finest("Setting auto commit to " + autoCommit + ".");

                connection.setAutoCommit(autoCommit);
            } catch (SQLException e) {
                throw new OsmosisRuntimeException("Unable to commit changes.", e);
            }
        }
        this.autoCommit = autoCommit;
    }

    /**
     * Indicates if the specified column exists in the database.
     * 
     * @param tableName The table to check for.
     * @param columnName The column to check for.
     * @return True if the column exists, false otherwise.
     */
    public boolean doesColumnExist(String tableName, String columnName) {
        ResultSet resultSet = null;
        boolean result;

        try {
            LOG.finest("Checking if column {" + columnName + "} in table {" + tableName + "} exists.");

            resultSet = getConnection().getMetaData().getColumns(null, null, tableName, columnName);
            result = resultSet.next();
            resultSet.close();
            resultSet = null;

            return result;

        } catch (SQLException e) {
            throw new OsmosisRuntimeException("Unable to check for the existence of column " + tableName + "."
                    + columnName + ".", e);
        } finally {
            if (resultSet != null) {
                try {
                    resultSet.close();
                } catch (SQLException e) {
                    // We are already in an error condition so log and continue.
                    LOG.log(Level.WARNING, "Unable to close column existence result set.", e);
                }
            }
        }
    }

    /**
     * Indicates if the specified table exists in the database.
     * 
     * @param tableName The table to check for.
     * @return True if the table exists, false otherwise.
     */
    public boolean doesTableExist(String tableName) {
        ResultSet resultSet = null;
        boolean result;

        try {
            LOG.finest("Checking if table {" + tableName + "} exists.");

            resultSet = getConnection().getMetaData().getTables(null, null, tableName, new String[] {"TABLE"});
            result = resultSet.next();
            resultSet.close();
            resultSet = null;

            return result;

        } catch (SQLException e) {
            throw new OsmosisRuntimeException("Unable to check for the existence of table " + tableName + ".", e);
        } finally {
            if (resultSet != null) {
                try {
                    resultSet.close();
                } catch (SQLException e) {
                    // We are already in an error condition so log and continue.
                    LOG.log(Level.WARNING, "Unable to close table existence result set.", e);
                }
            }
        }
    }

    /**
     * Enforces cleanup of any remaining resources during garbage collection. This is a safeguard
     * and should not be required if release is called appropriately.
     * 
     * @throws Throwable If a problem occurs during finalization.
     */
    @Override
    protected void finalize() throws Throwable {
        release();

        super.finalize();
    }

}
