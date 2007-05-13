package com.bretth.osm.conduit.mysql.impl;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.bretth.osm.conduit.ConduitRuntimeException;


/**
 * This class manages the lifecycle of JDBC objects to minimise the risk of
 * connection leaks and to support a consistent approach to database access.
 * 
 * @author Brett Henderson
 */
public class DatabaseContext {
	private static boolean driverLoaded;
	
	private String host;
	private String database;
	private String user;
	private String password;
	private Connection connection;
	private Statement statement;
	private ResultSet resultSet;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param host
	 *            The server hosting the database.
	 * @param database
	 *            The database instance.
	 * @param user
	 *            The user name for authentication.
	 * @param password
	 *            The password for authentication.
	 */
	public DatabaseContext(String host, String database, String user, String password) {
		this.host = host;
		this.database = database;
		this.user = user;
		this.password = password;
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
						Class.forName("com.mysql.jdbc.Driver");
						
					} catch (ClassNotFoundException e) {
						throw new ConduitRuntimeException("Unable to find database driver.", e);
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
					"jdbc:mysql://" + host + "/" + database + "?"
			    	+ "user=" + user + "&password=" + password
			    );
				
			} catch (SQLException e) {
				throw new ConduitRuntimeException("Unable to establish a database connection.", e);
			}
		}
		
		return connection;
	}
	
	
	/**
	 * Releases any open database statement.
	 */
	private void releaseStatement() {
		if (statement != null) {
			try {
				statement.close();
				
			} catch (SQLException e) {
				// Do nothing.
			}
			
			statement = null;
		}
	}
	
	
	/**
	 * Creates a new database prepared statement. Any existing statement will be
	 * closed.
	 * 
	 * @param sql
	 *            The statement to be created.
	 * @return The newly created statement.
	 */
	public PreparedStatement prepareStatement(String sql) {
		releaseStatement();
		
		try {
			PreparedStatement preparedStatement;
			
			preparedStatement = getConnection().prepareStatement(sql);
			
			statement = preparedStatement;
			
			return preparedStatement;
			
		} catch (SQLException e) {
			throw new ConduitRuntimeException("Unable to create database prepared statement.", e);
		}
	}
	
	
	/**
	 * Releases any open result set.
	 */
	private void releaseResultSet() {
		if (resultSet != null) {
			try {
				resultSet.close();
				
			} catch (SQLException e) {
				// Do nothing.
			}
			
			resultSet = null;
		}
	}
	
	
	/**
	 * Creates a result set that is configured to stream results from the
	 * database.  Any existing result set will be closed.
	 * 
	 * @param sql
	 *            The query to invoke.
	 * @return The result set.
	 */
	public ResultSet executeStreamingQuery(String sql) {
		releaseResultSet();
		releaseStatement();
		
		try {
			// Create a statement for returning streaming results.
			statement = getConnection().createStatement(
					ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
			
			statement.setFetchSize(Integer.MIN_VALUE);
			
			resultSet = statement.executeQuery(sql);
			
			return resultSet;
			
		} catch (SQLException e) {
			throw new ConduitRuntimeException("Unable to create streaming resultset statement.", e);
		}
	}
	
	
	/**
	 * Commits any outstanding transaction.
	 */
	public void commit() {
		// Not using transactions yet.
	}
	
	
	/**
	 * Releases all database resources. This method is guaranteed not to throw
	 * transactions and should always be called in a finally block whenever this
	 * class is used.
	 */
	public void release() {
		releaseResultSet();
		releaseStatement();
		
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
