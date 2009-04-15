// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package org.openstreetmap.osmosis.core.apidb.common;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.openstreetmap.osmosis.core.OsmosisRuntimeException;
import org.openstreetmap.osmosis.core.database.DatabaseLoginCredentials;


/**
 * This class manages the lifecycle of JDBC objects to minimise the risk of
 * connection leaks and to support a consistent approach to database access.
 * 
 * @author Brett Henderson
 */
public class DatabaseContext {
	
	private static final Logger LOG = Logger.getLogger(DatabaseContext.class.getName());
	
	static {
		// Register the database driver.
		try {
			Class.forName("com.mysql.jdbc.Driver");
			
		} catch (ClassNotFoundException e) {
			throw new OsmosisRuntimeException("Unable to find database driver.", e);
		}
	}
	
	
	private DatabaseLoginCredentials loginCredentials;
	private Connection connection;
	/**
	 * This statement is used by streaming result sets. It is stored globally
	 * here to allow it to remain open after a method return. It will be closed
	 * during release or if a new streaming result set is created.
	 */
	private Statement streamingStatement;
	
	
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
	 * If no database connection is open, a new connection is opened. The
	 * database connection is then returned.
	 * 
	 * @return The database connection.
	 */
	private Connection getConnection() {
		if (connection == null) {
			
			try {
				String url;
				
				url =
					"jdbc:mysql://" + loginCredentials.getHost() + "/"
					+ loginCredentials.getDatabase()
					+ "?user=" + loginCredentials.getUser()
					+ "&password=" + loginCredentials.getPassword();
				
				if (loginCredentials.getForceUtf8()) {
					url += "&useUnicode=true&characterEncoding=UTF-8";
				}
				if (loginCredentials.getProfileSql()) {
					url += "&profileSql=true";
				}
				
				connection = DriverManager.getConnection(url);
				
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
			Statement statement;
			
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
	 * Creates a new database statement that is configured so that any result
	 * sets created using it will stream data from the database instead of
	 * returning all records at once and storing in memory.
	 * <p>
	 * If no input parameters need to be set on the statement, use the
	 * executeStreamingQuery method instead.
	 * 
	 * @param sql
	 *            The statement to be created. This must be a select statement.
	 * @return The newly created statement.
	 */
	public PreparedStatement prepareStatementForStreaming(String sql) {
		try {
			PreparedStatement statement;
			
			// Create a statement for returning streaming results.
			statement = getConnection().prepareStatement(
					sql,
					ResultSet.TYPE_FORWARD_ONLY,
					ResultSet.CONCUR_READ_ONLY);
			
			statement.setFetchSize(Integer.MIN_VALUE);
			
			return statement;
			
		} catch (SQLException e) {
			throw new OsmosisRuntimeException("Unable to create streaming resultset statement.", e);
		}
	}
	
	
	/**
	 * Creates a result set that is configured to stream results from the
	 * database.
	 * 
	 * @param sql
	 *            The query to invoke.
	 * @return The result set.
	 */
	public ResultSet executeStreamingQuery(String sql) {
		try {
			ResultSet resultSet;
			
			// Close any existing streaming statement.
			if (streamingStatement != null) {
				try {
					streamingStatement.close();
					streamingStatement = null;
					
				} catch (SQLException e) {
					throw new OsmosisRuntimeException("Unable to close existing streaming statement.", e);
				}
			}
			
			// Create a statement for returning streaming results.
			streamingStatement = getConnection().createStatement(
					ResultSet.TYPE_FORWARD_ONLY,
					ResultSet.CONCUR_READ_ONLY);
			
			streamingStatement.setFetchSize(Integer.MIN_VALUE);
			
			resultSet = streamingStatement.executeQuery(sql);
			
			return resultSet;
			
		} catch (SQLException e) {
			throw new OsmosisRuntimeException("Unable to create streaming resultset.", e);
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
		if (streamingStatement != null) {
			try {
				streamingStatement.close();
				
			} catch (SQLException e) {
				// We cannot throw an exception within a release statement.
				LOG.log(Level.WARNING, "Unable to close existing streaming statement.", e);
			}
			
			streamingStatement = null;
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
	 * Enforces cleanup of any remaining resources during garbage collection.
	 * This is a safeguard and should not be required if release is called
	 * appropriately.
	 * 
	 * @throws Throwable
	 *             If a problem occurs during finalization.
	 */
	@Override
	protected void finalize() throws Throwable {
		release();
		
		super.finalize();
	}
}
