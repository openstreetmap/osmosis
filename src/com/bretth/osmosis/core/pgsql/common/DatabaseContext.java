// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package com.bretth.osmosis.core.pgsql.common;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.bretth.osmosis.core.OsmosisRuntimeException;
import com.bretth.osmosis.core.database.DatabaseLoginCredentials;


/**
 * This class manages the lifecycle of JDBC objects to minimise the risk of
 * connection leaks and to support a consistent approach to database access.
 * 
 * @author Brett Henderson
 */
public class DatabaseContext {
	private static boolean driverLoaded;
	
	private DatabaseLoginCredentials loginCredentials;
	private Connection connection;
	Statement statement;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param loginCredentials
	 *            Contains all information required to connect to the database.
	 */
	public DatabaseContext(DatabaseLoginCredentials loginCredentials) {
		this.loginCredentials = loginCredentials;
	}
	
	
	/**
	 * Utility method for ensuring that the database driver is registered.
	 */
	private static void loadDatabaseDriver() {
		if (!driverLoaded) {
			// Lock to ensure two threads don't try to load the driver at the same time.
			synchronized (DatabaseContext.class) {
				// Check again to ensure another thread hasn't loaded the driver
				// while we waited for the lock.
				if (!driverLoaded) {
					try {
						Class.forName("org.postgresql.Driver");
						
					} catch (ClassNotFoundException e) {
						throw new OsmosisRuntimeException("Unable to find database driver.", e);
					}
					
					driverLoaded = true;
				}
			}
		}
	}
	
	
	/**
	 * If no database connection is open, a new connection is opened. The
	 * database connection is then returned.
	 * 
	 * @return The database connection.
	 */
	private Connection getConnection() {
		if (connection == null) {
			
			loadDatabaseDriver();
			
			try {
				connection = DriverManager.getConnection(
					"jdbc:postgresql://" + loginCredentials.getHost() + "/"
					+ loginCredentials.getDatabase()
			    	+ "?user=" + loginCredentials.getUser()
			    	+ "&password=" + loginCredentials.getPassword()// + "&logLevel=2"
			    );
				
				connection.setAutoCommit(false);
				
			} catch (SQLException e) {
				throw new OsmosisRuntimeException("Unable to establish a database connection.", e);
			}
		}
		
		return connection;
	}
	
	
	/**
	 * Executes a sql statement against the database.
	 * 
	 * @param sql
	 *            The sql statement to be invoked.
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
	
	
	/**
	 * Creates a new database callable statement.
	 * 
	 * @param sql
	 *            The statement to be created.
	 * @return The newly created statement.
	 */
	public CallableStatement prepareCall(String sql) {
		try {
			CallableStatement callableStatement;
			
			callableStatement = getConnection().prepareCall(sql);
			
			return callableStatement;
			
		} catch (SQLException e) {
			throw new OsmosisRuntimeException("Unable to create database callable statement.", e);
		}
	}
	
	
	/**
	 * Creates a new database statement.
	 * 
	 * @return The newly created statement.
	 */
	public Statement createStatement() {
		try {
			Statement resultStatement;
			
			resultStatement = getConnection().createStatement();
			
			return resultStatement;
			
		} catch (SQLException e) {
			throw new OsmosisRuntimeException("Unable to create database statement.", e);
		}
	}
	
	
	/**
	 * Executes a query and returns a result set. The returned result set must
	 * be closed by the caller.
	 * 
	 * @param sql
	 *            The query to execute.
	 * @return The newly created result set.
	 */
	public ResultSet executeQuery(String sql) {
		try {
			ResultSet resultSet;
			
			if (statement != null) {
				statement.close();
			}
			
			statement = getConnection().createStatement(
				ResultSet.TYPE_FORWARD_ONLY,
				ResultSet.CONCUR_READ_ONLY
			);
			statement.setFetchSize(10000);
			
			resultSet = statement.executeQuery(sql);
			
			return resultSet;
			
		} catch (SQLException e) {
			throw new OsmosisRuntimeException("Unable to create resultset.", e);
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
		ResultSet resultSet = null;
		boolean result;
		
		try {
			resultSet = getConnection().getMetaData().getColumns(null, null, tableName, columnName);
			result = resultSet.next();
			resultSet.close();
			resultSet = null;
			
			return result;
			
		} catch (SQLException e) {
			throw new OsmosisRuntimeException(
				"Unable to check for the existence of column " + tableName + "." + columnName + ".",
				e
			);
		} finally {
			if (resultSet != null) {
				try {
					resultSet.close();
				} catch (SQLException e) {
					// Do nothing.
				}
			}
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
	 * Releases all database resources. This method is guaranteed not to throw
	 * transactions and should always be called in a finally block whenever this
	 * class is used.
	 */
	public void release() {
		if (connection != null) {
			try {
				connection.close();
				
			} catch (SQLException e) {
				// Do nothing.
			}
			
			connection = null;
		}
	}
	
	
	/**
	 * Enforces cleanup of any remaining resources during garbage collection.
	 * This is a safeguard and should not be required if release is called
	 * appropriately.
	 */
	@Override
	protected void finalize() throws Throwable {
		release();
		
		super.finalize();
	}
}
