package com.bretth.osmosis.core.mysql.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.NoSuchElementException;

import com.bretth.osmosis.core.OsmosisRuntimeException;
import com.bretth.osmosis.core.store.ReleasableIterator;


/**
 * Provides the base implementation of all database entity readers.
 * 
 * @author Brett Henderson
 * 
 * @param <T>
 *            The type of entity to retrieved.
 */
public abstract class BaseEntityReader<T> implements ReleasableIterator<T> {
	
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
	public BaseEntityReader(String host, String database, String user, String password) {
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
	 * {@inheritDoc}
	 */
	public boolean hasNext() {
		if (resultSet == null) {
			readNextValue();
		}
		
		return (nextValue != null);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public T next() {
		T result;
		
		if (!hasNext()) {
			throw new NoSuchElementException();
		}
		
		result = nextValue;
		
		readNextValue();
		
		return result;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void release() {
		nextValue = null;
		resultSet = null;
		
		dbCtx.release();
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void remove() {
		throw new UnsupportedOperationException();
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
