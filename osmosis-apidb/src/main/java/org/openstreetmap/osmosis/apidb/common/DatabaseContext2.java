// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.apidb.common;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.sql.DataSource;

import org.apache.commons.dbcp.BasicDataSource;
import org.openstreetmap.osmosis.core.OsmosisRuntimeException;
import org.openstreetmap.osmosis.core.database.DatabaseLoginCredentials;
import org.openstreetmap.osmosis.core.database.DatabaseType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;


/**
 * This class manages the lifecycle of JDBC objects to minimise the risk of connection leaks and to
 * support a consistent approach to database access.
 * 
 * @author Brett Henderson
 */
public class DatabaseContext2 implements AutoCloseable {

    private static final Logger LOG = Logger.getLogger(DatabaseContext.class.getName());

    private BasicDataSource dataSource;
    private PlatformTransactionManager txnManager;
    private TransactionTemplate txnTemplate;
    private JdbcTemplate jdbcTemplate;
    private DatabaseType dbType;
    private IdentityValueLoader identityValueLoader;
    

    /**
     * Creates a new instance.
     * 
     * @param loginCredentials Contains all information required to connect to the database.
     */
    public DatabaseContext2(DatabaseLoginCredentials loginCredentials) {
    	dataSource = DataSourceFactory.createDataSource(loginCredentials);
    	txnManager = new DataSourceTransactionManager(dataSource);
    	txnTemplate = new TransactionTemplate(txnManager);
    	jdbcTemplate = new JdbcTemplate(dataSource);
    	this.dbType = loginCredentials.getDbType();
    	
    	setStatementFetchSizeForStreaming();

        switch (loginCredentials.getDbType()) {
        case POSTGRESQL:
            identityValueLoader = new PostgresqlIdentityValueLoader2(this);
            break;
        case MYSQL:
            identityValueLoader = new MysqlIdentityValueLoader2(this);
            break;
        default:
			throw createUnknownDbTypeException();
        }
    }

	/**
	 * Retrieves the data source associated with this database context.
	 *
	 * @return {@link DataSource}
	 */
	public DataSource getDataSource() {
		return this.dataSource;
	}

	private OsmosisRuntimeException createUnknownDbTypeException() {
		return new OsmosisRuntimeException("Unknown database type " + dbType + ".");
	}
    
    
    /**
     * Gets the jdbc template which provides access to database functions.
     * 
     * @return The jdbc template.
     */
    public JdbcTemplate getJdbcTemplate() {
    	return jdbcTemplate;
    }
    
    
	/**
	 * Invokes the provided callback code within a transaction.
	 * 
	 * @param txnCallback
	 *            The logic to be invoked within a transaction.
	 * @param <T>
	 *            The return type of the transaction callback.
	 * 
	 * @return The result.
	 */
    public <T> Object executeWithinTransaction(TransactionCallback<T> txnCallback) {
    	return txnTemplate.execute(txnCallback);
    }


	/**
	 * Returns the database type currently in use. This should only be used when it is not possible
	 * to write database agnostic statements.
	 * 
	 * @return The database type.
	 */
    public DatabaseType getDatabaseType() {
    	return dbType;
    }
    
    
    private void setStatementFetchSizeForStreaming() {
    	switch (dbType) {
        case POSTGRESQL:
        	jdbcTemplate.setFetchSize(10000);
			break;
        case MYSQL:
        	jdbcTemplate.setFetchSize(Integer.MIN_VALUE);
			break;
		default:
			throw createUnknownDbTypeException();
		}
    }
	
	
    /**
	 * Truncates the contents of the specified tables.
	 * 
	 * @param tables
	 *            The tables to be truncated.
	 */
	public void truncateTables(List<String> tables) {
		switch (dbType) {
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
			
			jdbcTemplate.update(statementBuilder.toString());
			break;
        case MYSQL:
			for (String table : tables) {
				jdbcTemplate.update("TRUNCATE " + table);
			}
			break;
		default:
			throw createUnknownDbTypeException();
		}
	}
	
	
    /**
	 * Disables the indexes of the specified tables.
	 * 
	 * @param tables
	 *            The tables to disable indexes on.
	 */
	public void disableIndexes(List<String> tables) {
		switch (dbType) {
        case POSTGRESQL:
			// There is no way to automatically disable all indexes for a table.
			break;
        case MYSQL:
        	for (String table : tables) {
        		jdbcTemplate.update("ALTER TABLE " + table + " DISABLE KEYS");
			}
			break;
		default:
			throw createUnknownDbTypeException();
		}
	}
	
	
    /**
	 * Enables the indexes of the specified tables.
	 * 
	 * @param tables
	 *            The tables to enable indexes on.
	 */
	public void enableIndexes(List<String> tables) {
		switch (dbType) {
        case POSTGRESQL:
			// There is no way to automatically disable all indexes for a table.
			break;
        case MYSQL:
        	for (String table : tables) {
        		jdbcTemplate.update("ALTER TABLE " + table + " ENABLE KEYS");
			}
			break;
		default:
			throw createUnknownDbTypeException();
		}
	}
	
	
    /**
	 * Locks the specified tables for exclusive access.
	 * 
	 * @param tables
	 *            The tables to lock.
	 */
	public void lockTables(List<String> tables) {
		switch (dbType) {
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
        	
        	jdbcTemplate.update(statementBuilder.toString());
			break;
		default:
			throw createUnknownDbTypeException();
		}
	}
	
	
    /**
	 * Unlocks the specified tables.
	 * 
	 * @param tables
	 *            The tables to unlock.
	 */
	public void unlockTables(List<String> tables) {
		switch (dbType) {
        case POSTGRESQL:
			// Locking tables is not supported.
			break;
        case MYSQL:
        	jdbcTemplate.update("UNLOCK TABLES");
			break;
		default:
			throw createUnknownDbTypeException();
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
     * Releases all database resources. This method is guaranteed not to throw transactions and
     * should always be called in a finally block whenever this class is used.
     * 
     * @deprecated Use {@link #close()} instead.
     */
    public void release() {
    	close();
    }
    
    
    /**
     * Releases all database resources. This method is guaranteed not to throw transactions and
     * should always be called in a finally or try-with-resources block whenever this class is used.
     */
    public void close() {
    	identityValueLoader.close();
    	
    	try {
			dataSource.close();
		} catch (SQLException e) {
			LOG.log(Level.WARNING, "Unable to cleanup the database connection pool.", e);
		}
    }
    

    /**
     * Indicates if the specified column exists in the database.
     * 
     * @param tableName The table to check for.
     * @param columnName The column to check for.
     * @return True if the column exists, false otherwise.
     */
	public boolean doesColumnExist(String tableName, String columnName) {
		Connection connection;

		LOG.finest("Checking if column {" + columnName + "} in table {" + tableName + "} exists.");

		connection = DataSourceUtils.getConnection(dataSource);
		try {
			try (ResultSet resultSet = connection.getMetaData().getColumns(null, null, tableName, columnName)) {
				return resultSet.next();
			} catch (SQLException e) {
				throw new OsmosisRuntimeException("Unable to check for the existence of column " + tableName + "."
						+ columnName + ".", e);
			}
		} finally {
			DataSourceUtils.releaseConnection(connection, dataSource);
		}
	}

    /**
     * Indicates if the specified table exists in the database.
     * 
     * @param tableName The table to check for.
     * @return True if the table exists, false otherwise.
     */
    public boolean doesTableExist(String tableName) {
    	Connection connection;

    	LOG.finest("Checking if table {" + tableName + "} exists.");

    	connection = DataSourceUtils.getConnection(dataSource);
		try (ResultSet resultSet =
					 connection.getMetaData().getTables(null, null, tableName, new String[] {"TABLE"})) {
			return resultSet.next();
		} catch (SQLException e) {
			throw new OsmosisRuntimeException("Unable to check for the existence of table " + tableName + ".", e);
		} finally {
			DataSourceUtils.releaseConnection(connection, dataSource);
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
        close();

        super.finalize();
    }

}
