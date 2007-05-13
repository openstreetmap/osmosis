package com.bretth.osm.conduit.mysql.impl;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.bretth.osm.conduit.ConduitRuntimeException;


/**
 * Provides iterator like behaviour for reading entities from a database.
 * 
 * @author Brett Henderson
 * 
 * @param <EntityType>
 *            The type of entity to retrieved.
 */
public abstract class EntityReader<EntityType> {
	
	private DatabaseContext dbCtx;
	private ResultSet resultSet;
	private EntityType nextValue;
	
	
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
	public EntityReader(String host, String database, String user, String password) {
		dbCtx = new DatabaseContext(host, database, user, password);
	}
	
	
	/**
	 * Provides the sql query to retrieve the entity records from the database.
	 * 
	 * @return The sql query.
	 */
	protected abstract String getQuerySql();
	
	
	/**
	 * Builds an entity object from the current recordset row.
	 * 
	 * @param activeResultSet
	 *            The record set to retrieve the data from.
	 * @return The entity object.
	 */
	protected abstract EntityType createNextValue(ResultSet activeResultSet);
	
	
	/**
	 * Reads the next entity from the database and stores it in the internal
	 * nextValue variable. This will be set to null if no more data is
	 * available.
	 */
	private void readNextValue() {
		if (resultSet == null) {
			resultSet = dbCtx.executeStreamingQuery(getQuerySql());
		}
		
		try {
			if (resultSet.next()) {
				nextValue = createNextValue(resultSet);
			} else {
				nextValue = null;
			}
			
		} catch (SQLException e) {
			throw new ConduitRuntimeException("Unable to move to next record.", e);
		}
	}
	
	
	/**
	 * Indicates if there is any more data available to be read.
	 * 
	 * @return True if more data is available, false otherwise.
	 */
	public boolean hasNext() {
		if (resultSet == null) {
			readNextValue();
		}
		
		return (nextValue != null);
	}
	
	
	/**
	 * Returns the next available entity without advancing to the next record.
	 * 
	 * @return The next available entity.
	 */
	public EntityType peekNext() {
		if (resultSet == null) {
			throw new ConduitRuntimeException("hasNext must be called first.");
		}
		if (nextValue == null) {
			throw new ConduitRuntimeException("No value is available.");
		}
		
		return nextValue;
	}
	
	
	/**
	 * Returns the next available entity and advances to the next record.
	 * 
	 * @return The next available entity.
	 */
	public EntityType next() {
		EntityType result;
		
		result = peekNext();
		
		readNextValue();
		
		return result;
	}
	
	
	/**
	 * Releases all database resources. This method is guaranteed not to throw
	 * transactions and should always be called in a finally block whenever this
	 * class is used.
	 */
	public void release() {
		nextValue = null;
		resultSet = null;
		
		dbCtx.release();
	}
}
