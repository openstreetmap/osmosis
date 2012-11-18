// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.pgsnapshot.common;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.sql.DataSource;

import org.openstreetmap.osmosis.core.OsmosisRuntimeException;
import org.openstreetmap.osmosis.core.database.DatabaseLoginCredentials;
import org.postgresql.copy.CopyManager;
import org.postgresql.core.BaseConnection;
import org.springframework.jdbc.core.JdbcTemplate;
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
public class DatabaseContext {

    private static final Logger LOG = Logger.getLogger(DatabaseContext.class.getName());

    private DataSourceManager dataSourceManager;
    private DataSource dataSource;
    private PlatformTransactionManager txnManager;
    private TransactionTemplate txnTemplate;
    private TransactionStatus transaction;
    private JdbcTemplate jdbcTemplate;
    

    /**
     * Creates a new instance.
     * 
     * @param loginCredentials Contains all information required to connect to the database.
     */
    public DatabaseContext(DatabaseLoginCredentials loginCredentials) {
    	dataSourceManager = new DataSourceManager(loginCredentials);
    	dataSource = dataSourceManager.getDataSource();
    	txnManager = new DataSourceTransactionManager(dataSource);
    	txnTemplate = new TransactionTemplate(txnManager);
    	jdbcTemplate = new JdbcTemplate(dataSource);
    	
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
	 * Loads a table from a COPY file.
	 * 
	 * @param copyFile
	 *            The file to be loaded.
	 * @param tableName
	 *            The table to load the data into.
	 * @param columns
	 *            The columns to be loaded (optional).
	 */
    public void loadCopyFile(File copyFile, String tableName, String ... columns) {
    	CopyManager copyManager;
    	InputStream inStream = null;
    	
    	try {
    		StringBuilder copyStatement;
    		InputStream bufferedInStream;
    		Connection conn;
    		
    		copyStatement = new StringBuilder();
    		copyStatement.append("COPY ");
    		copyStatement.append(tableName);
    		if (columns.length > 0) {
    			copyStatement.append('(');
    			for (int i = 0; i < columns.length; i++) {
    				if (i > 0) {
    					copyStatement.append(',');
    				}
    				copyStatement.append(columns[i]);
    			}
    			copyStatement.append(')');
    		}
    		copyStatement.append(" FROM STDIN");
    		
    		inStream = new FileInputStream(copyFile);
    		bufferedInStream = new BufferedInputStream(inStream, 65536);
    		
    		conn = DataSourceUtils.getConnection(dataSource);
    		try {
	    		copyManager = new CopyManager(conn.unwrap(BaseConnection.class));
	    		
	    		copyManager.copyIn(copyStatement.toString(), bufferedInStream);
    		} finally {
    			DataSourceUtils.releaseConnection(conn, dataSource);
    		}
			
    		inStream.close();
			inStream = null;
			
    	} catch (IOException e) {
    		throw new OsmosisRuntimeException("Unable to process COPY file " + copyFile + ".", e);
    	} catch (SQLException e) {
    		throw new OsmosisRuntimeException("Unable to process COPY file " + copyFile + ".", e);
    	} finally {
    		if (inStream != null) {
				try {
					inStream.close();
				} catch (IOException e) {
					LOG.log(Level.SEVERE, "Unable to close COPY file.", e);
				}
				inStream = null;
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
