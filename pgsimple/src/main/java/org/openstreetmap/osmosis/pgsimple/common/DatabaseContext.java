// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.pgsimple.common;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

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
			Class.forName("org.postgresql.Driver");
			
		} catch (ClassNotFoundException e) {
			throw new OsmosisRuntimeException("Unable to find database driver.", e);
		}
	}
	
	
	private DatabaseLoginCredentials loginCredentials;
	private Connection connection;
	private boolean autoCommit;
	private Statement statement;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param loginCredentials
	 *            Contains all information required to connect to the database.
	 */
	public DatabaseContext(DatabaseLoginCredentials loginCredentials) {
		this.loginCredentials = loginCredentials;
		
		autoCommit = false;
	}
	
	
	private Connection getConnectionFromDriverManager() {
		try {
			return DriverManager.getConnection(
				"jdbc:postgresql://" + loginCredentials.getHost() + "/"
				+ loginCredentials.getDatabase(),
		    	// + "?logLevel=2"
		    	loginCredentials.getUser(),
		    	loginCredentials.getPassword()
		    );
			
		} catch (SQLException e) {
			throw new OsmosisRuntimeException("Unable to establish a new database connection.", e);
		}
	}
	
	
	private Connection getConnectionFromDatasource() {
		InitialContext cxt;
		DataSource ds;
		String jndiLocation;
		
		jndiLocation = loginCredentials.getDatasourceJndiLocation();
		
		try {
			cxt = new InitialContext();
		} catch (NamingException e) {
			throw new OsmosisRuntimeException("Unable to create an initial JNDI context.", e);
		}
		
		try {
			ds = (DataSource) cxt.lookup(jndiLocation);
		} catch (NamingException e) {
			throw new OsmosisRuntimeException("Unable to locate the datasource (" + jndiLocation + ")", e);
		}

		try {
			return ds.getConnection();
		} catch (SQLException e) {
			throw new OsmosisRuntimeException("Unable to obtain a connection from the datasource.", e);
		}
	}
	
	
	/**
	 * If no database connection is open, a new connection is opened. The
	 * database connection is then returned.
	 * 
	 * @return The database connection.
	 */
	public Connection getConnection() {
		if (connection == null) {
			String jndiLocation;
			
			jndiLocation = loginCredentials.getDatasourceJndiLocation();
			
			if (jndiLocation != null) {
				LOG.finer("Creating a new database connection from JNDI.");
				
				connection = getConnectionFromDatasource();
				
			} else {
				LOG.finer("Creating a new database connection using DriverManager.");
				
				connection = getConnectionFromDriverManager();
			}
			
			try {
				connection.setAutoCommit(autoCommit);
				
			} catch (SQLException e) {
				throw new OsmosisRuntimeException("Unable to set auto commit to " + autoCommit + ".", e);
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
			LOG.finest("Executing statement {" + sql + "}");
			
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
			
			LOG.finest("Creating prepared statement {" + sql + "}");
			
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
			
			LOG.finest("Creating callable statement {" + sql + "}");
			
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
			
			LOG.finest("Creating a new statement.");
			
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
			
			LOG.finest("Executing query {" + sql + "}");
			
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
			LOG.finest("Checking if column {" + columnName + "} in table {" + tableName + "} exists.");
			
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
			
			resultSet = getConnection().getMetaData().getTables(null, null, tableName, new String[]{"TABLE"});
			result = resultSet.next();
			resultSet.close();
			resultSet = null;
			
			return result;
			
		} catch (SQLException e) {
			throw new OsmosisRuntimeException(
				"Unable to check for the existence of table " + tableName + ".",
				e
			);
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
	 * Sets the auto-commit property on the underlying connection.
	 * 
	 * @param autoCommit
	 *            The new auto commit value.
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
	 * Commits any outstanding transaction.
	 */
	public void commit() {
		if (connection != null) {
			try {
				LOG.finest("Committing changes.");
				
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
				LOG.finest("Closing the database connection.");
				
				connection.close();
				
			} catch (SQLException e) {
				// We cannot throw an exception within a release statement.
				LOG.log(Level.WARNING, "Unable to close result set.", e);
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
	 *             if an unexpected problem occurs during finalization.
	 */
	@Override
	protected void finalize() throws Throwable {
		release();
		
		super.finalize();
	}
}
