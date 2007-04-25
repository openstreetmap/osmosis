package com.bretth.osm.conduit.mysql.impl;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.bretth.osm.conduit.ConduitRuntimeException;


public class DatabaseContext {
	private static boolean driverLoaded;
	
	private String host;
	private String database;
	private String user;
	private String password;
	private Connection connection;
	private Statement statement;
	private ResultSet resultSet;
	
	
	public DatabaseContext(String host, String database, String user, String password) {
		this.host = host;
		this.database = database;
		this.user = user;
		this.password = password;
	}
	
	
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
	
	
	public void commit() {
		// Not using transactions yet.
	}
	
	
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
	
	
	@Override
	protected void finalize() throws Throwable {
		release();
		
		super.finalize();
	}
}
