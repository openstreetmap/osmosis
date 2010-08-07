// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.pgsnapshot.common;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.sql.DataSource;

import org.openstreetmap.osmosis.core.OsmosisRuntimeException;
import org.openstreetmap.osmosis.core.database.DatabaseLoginCredentials;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;


/**
 * This class manages the lifecycle of JDBC objects to minimise the risk of connection leaks and to
 * support a consistent approach to database access.
 * 
 * @author Brett Henderson
 */
public class DatabaseContext2 {

    private static final Logger LOG = Logger.getLogger(DatabaseContext.class.getName());

    private DataSourceManager dataSourceManager;
    private DataSource dataSource;
    private PlatformTransactionManager txnManager;
    private TransactionTemplate txnTemplate;
    private TransactionStatus transaction;
    private JdbcTemplate jdbcTemplate;
    private SimpleJdbcTemplate simpleJdbcTemplate;
    

    /**
     * Creates a new instance.
     * 
     * @param loginCredentials Contains all information required to connect to the database.
     */
    public DatabaseContext2(DatabaseLoginCredentials loginCredentials) {
    	dataSourceManager = new DataSourceManager(loginCredentials);
    	dataSource = dataSourceManager.getDataSource();
    	txnManager = new DataSourceTransactionManager(dataSource);
    	txnTemplate = new TransactionTemplate(txnManager);
    	jdbcTemplate = new JdbcTemplate(dataSource);
    	simpleJdbcTemplate = new SimpleJdbcTemplate(jdbcTemplate);
    	
    	setStatementFetchSizeForStreaming();
    }


	/**
	 * Begins a new database transaction. This is not required if
	 * executeWithinTransaction is being used.
	 */
    public void beginTransaction() {
    	if (transaction != null) {
    		throw new OsmosisRuntimeException("A transaction is already active.");
    	}
    	
    	transaction = txnManager.getTransaction(new DefaultTransactionDefinition());
    }
    
    
    /**
     * Commits an existing database transaction.
     */
    public void commitTransaction() {
    	if (transaction == null) {
    		throw new OsmosisRuntimeException("No transaction is currently active.");
    	}
    	
    	try {
    		txnManager.commit(transaction);
    	} finally {
    		transaction = null;
    	}
    }
    
    
    /**
     * Gets the jdbc template which provides simple access to database functions.
     * 
     * @return The jdbc template.
     */
    public SimpleJdbcTemplate getSimpleJdbcTemplate() {
    	return simpleJdbcTemplate;
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
    
    
    private void setStatementFetchSizeForStreaming() {
        jdbcTemplate.setFetchSize(10000);
    }
	

    /**
     * Releases all database resources. This method is guaranteed not to throw transactions and
     * should always be called in a finally block whenever this class is used.
     */
    public void release() {
    	if (transaction != null) {
    		try {
    			txnManager.rollback(transaction);
    		} finally {
    			transaction = null;
    		}
    	}
    	
    	dataSourceManager.release();
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
        	Connection connection;
        	
            LOG.finest("Checking if column {" + columnName + "} in table {" + tableName + "} exists.");

            // This connection may not be freed if an exception occurs. It's a small chance and the
			// additional code to avoid it is cumbersome.
            connection = DataSourceUtils.getConnection(dataSource);
            
            resultSet = connection.getMetaData().getColumns(null, null, tableName, columnName);
            result = resultSet.next();
            resultSet.close();
            resultSet = null;
            
            DataSourceUtils.releaseConnection(connection, dataSource);

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
        	Connection connection;
        	
            LOG.finest("Checking if table {" + tableName + "} exists.");

            // This connection may not be freed if an exception occurs. It's a small chance and the
			// additional code to avoid it is cumbersome.
            connection = DataSourceUtils.getConnection(dataSource);

            resultSet = connection.getMetaData().getTables(null, null, tableName, new String[] {"TABLE"});
            result = resultSet.next();
            resultSet.close();
            resultSet = null;
            
            DataSourceUtils.releaseConnection(connection, dataSource);

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
