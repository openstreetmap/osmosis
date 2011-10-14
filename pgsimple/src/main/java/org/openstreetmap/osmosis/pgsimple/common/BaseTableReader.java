// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.pgsimple.common;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.NoSuchElementException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.openstreetmap.osmosis.core.OsmosisRuntimeException;
import org.openstreetmap.osmosis.core.lifecycle.ReleasableIterator;


/**
 * Provides the base implementation of all database table readers.
 * 
 * @author Brett Henderson
 * 
 * @param <T>
 *            The type of entity to retrieved.
 */
public abstract class BaseTableReader<T> implements ReleasableIterator<T> {
	private static final Logger LOG = Logger.getLogger(BaseTableReader.class.getName());
	private DatabaseContext dbCtx;
	private ResultSet resultSet;
	private T nextValue;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param dbCtx
	 *            The active connection to use for reading from the database.
	 */
	public BaseTableReader(DatabaseContext dbCtx) {
		this.dbCtx = dbCtx;
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
	 * If the implementation requires multiple rows to build an entity object,
	 * this method allows the implementation to return an entity based on the
	 * fact that no more rows are available. This default implementation returns
	 * a blank result.
	 * 
	 * @return The last result record.
	 */
	protected ReadResult<T> createLastValue() {
		return new ReadResult<T>(true, null);
	}
	
	
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
					
					readResult = createLastValue();
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
		if (resultSet != null) {
			try {
				resultSet.close();
			} catch (SQLException e) {
				// We cannot throw an exception within a release method.
				LOG.log(Level.WARNING, "Unable to close result set.", e);
			}
			
			resultSet = null;
		}
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
		 * @param usableResult
		 *            Indicates if this result should be used.
		 * @param entity
		 *            The entity being read.
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
