package com.bretth.osmosis.mysql.impl;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.bretth.osmosis.OsmosisRuntimeException;


/**
 * Provides iterator like behaviour for reading entities from a database.
 * 
 * @author Brett Henderson
 * 
 * @param <T>
 *            The type of entity to retrieved.
 */
public abstract class EntityReader<T> {
	
	private DatabaseContext dbCtx;
	private ResultSet resultSet;
	private T nextValue;
	
	
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
	 * Builds the result set that the reader will iterate over.
	 * 
	 * @param queryDbCtx
	 *            The database context to query against.
	 * @return A result set positioned before the first record.
	 */
	protected abstract ResultSet createResultSet(DatabaseContext queryDbCtx);
	
	
	/**
	 * Builds an entity object from the current recordset row.
	 * 
	 * @param activeResultSet
	 *            The record set to retrieve the data from.
	 * @return The result of the read.
	 */
	protected abstract ReadResult<T> createNextValue(ResultSet activeResultSet);
	
	
	/**
	 * Reads the next entity from the database and stores it in the internal
	 * nextValue variable. This will be set to null if no more data is
	 * available.
	 */
	private void readNextValue() {
		if (resultSet == null) {
			resultSet = createResultSet(dbCtx);
		}
		
		try {
			ReadResult<T> readResult;
			
			// Loop until a valid result is determined. Typically a loop is
			// required when a record on the result set is skipped over by the
			// reader implementation.
			do {
				if (resultSet.next()) {
					readResult = createNextValue(resultSet);
				} else {
					readResult = new ReadResult<T>(true, null);
				}
			} while (!readResult.isUsableResult());
			
			nextValue = readResult.getEntity();
			
		} catch (SQLException e) {
			throw new OsmosisRuntimeException("Unable to move to next record.", e);
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
	public T peekNext() {
		if (resultSet == null) {
			throw new OsmosisRuntimeException("hasNext must be called first.");
		}
		if (nextValue == null) {
			throw new OsmosisRuntimeException("No value is available.");
		}
		
		return nextValue;
	}
	
	
	/**
	 * Resets the state of the reader to the starting state. This allows a query
	 * to be re-issued without instantiating a new instance. Sub-classes may
	 * utilise this functionality to re-use existing prepared statements.
	 */
	public void reset() {
		if (resultSet != null) {
			try {
				resultSet.close();
			} catch (SQLException e) {
				throw new OsmosisRuntimeException("Unable to close the existing result set.", e);
			}
		}
		
		resultSet = null;
		nextValue = null;
	}
	
	
	/**
	 * Returns the next available entity and advances to the next record.
	 * 
	 * @return The next available entity.
	 */
	public T next() {
		T result;
		
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
	
	
	/**
	 * Represents the result of an entity read from the result set at the current position.
	 * 
	 * @param <T>
	 *            The type of entity to retrieved.
	 */
	protected static class ReadResult<T> {
		private boolean usableResult;
		private T entity;
		
		
		/**
		 * Creates a new instance.
		 * 
		 * @param usableResult Indicates if this result should be used.
		 * @param entity
		 */
		public ReadResult(boolean usableResult, T entity) {
			this.usableResult = usableResult;
			this.entity = entity;
		}
		
		
		/**
		 * Returns the usable result flag.
		 * 
		 * @return The usable result flag.
		 */
		public boolean isUsableResult() {
			return usableResult;
		}
		
		
		/**
		 * Returns the entity.
		 * 
		 * @return The entity.
		 */
		public T getEntity() {
			return entity;
		}
	}
}
