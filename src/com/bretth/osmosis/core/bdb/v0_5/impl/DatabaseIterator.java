// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package com.bretth.osmosis.core.bdb.v0_5.impl;

import java.util.NoSuchElementException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.bretth.osmosis.core.OsmosisRuntimeException;
import com.bretth.osmosis.core.store.ReleasableIterator;
import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.je.Cursor;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.OperationStatus;
import com.sleepycat.je.Transaction;


/**
 * An iterator for accessing the entire contents of a database.
 * 
 * @param <T>
 *            The type of data to be iterated over.
 * @author Brett Henderson
 */
public class DatabaseIterator<T> implements ReleasableIterator<T> {
	
	private static final Logger log = Logger.getLogger(DatabaseIterator.class.getName());
	
	private Database db;
	private Transaction txn;
	private TupleBinding dataBinding;
	private Cursor cursor;
	private boolean initialized;
	private T nextRecord;
	private boolean nextRecordAvailable;
	private DatabaseEntry keyEntry;
	private DatabaseEntry dataEntry;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param database
	 *            The database to read from.
	 * @param transaction
	 *            The active transaction.
	 * @param dataBinding
	 *            The binding allowing database data to be converted into the
	 *            result object.
	 */
	public DatabaseIterator(Database database, Transaction transaction, TupleBinding dataBinding) {
		this.db = database;
		this.txn = transaction;
		this.dataBinding = dataBinding;
		
		initialized = false;
		nextRecordAvailable = false;
		
		keyEntry = new DatabaseEntry();
		dataEntry = new DatabaseEntry();
	}
	
	
	private void initialize() {
		try {
			cursor = db.openCursor(txn, null);
		} catch (DatabaseException e) {
			throw new OsmosisRuntimeException("Unable to open database cursor.", e);
		}
		
		initialized = true;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	@Override
	public boolean hasNext() {
		if (!initialized) {
			initialize();
		}
		
		if (!nextRecordAvailable) {
			try {
				if (OperationStatus.SUCCESS.equals(cursor.getNext(keyEntry, dataEntry, null))) {
					nextRecord = (T) dataBinding.entryToObject(dataEntry);
					nextRecordAvailable = true;
				}
				
			} catch (DatabaseException e) {
				throw new OsmosisRuntimeException("Unable to read the next object from the database.", e);
			}
		}
		
		return nextRecordAvailable;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public T next() {
		if (!hasNext()) {
			throw new NoSuchElementException();
		}
		
		nextRecordAvailable = false;
		
		return nextRecord;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void release() {
		if (cursor != null) {
			try {
				cursor.close();
			} catch (DatabaseException e) {
				log.log(Level.SEVERE, "Unable to close database cursor.", e);
			}
			cursor = null;
		}
	}
}
