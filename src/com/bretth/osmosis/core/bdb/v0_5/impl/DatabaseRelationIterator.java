// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package com.bretth.osmosis.core.bdb.v0_5.impl;

import java.util.NoSuchElementException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.bretth.osmosis.core.OsmosisRuntimeException;
import com.bretth.osmosis.core.bdb.common.LongLongIndexElement;
import com.bretth.osmosis.core.bdb.common.StoreableTupleBinding;
import com.bretth.osmosis.core.store.ReleasableIterator;
import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.je.Cursor;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.OperationStatus;
import com.sleepycat.je.Transaction;


/**
 * The bdb schema used for OSM data uses several databases with two-part keys
 * relating two databases. This returns all values of the second part of the key
 * for all records matching the first part of the key.
 * 
 * @author Brett Henderson
 */
public class DatabaseRelationIterator implements ReleasableIterator<Long> {
	
	private static final Logger log = Logger.getLogger(DatabaseIterator.class.getName());
	
	private Database db;
	private Transaction txn;
	private long searchId;
	private TupleBinding keyBinding;
	private Cursor cursor;
	private boolean initialized;
	private Long nextRecord;
	private boolean nextRecordAvailable;
	private boolean cursorLive;
	private DatabaseEntry keyEntry;
	private DatabaseEntry dataEntry;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param database
	 *            The database to read from.
	 * @param transaction
	 *            The active transaction.
	 * @param searchId
	 *            Part 1 of the key identifying the range of records for which
	 *            to retrieve part 2 of the key. Part 1 represents the known id
	 *            for which we wish to retrieve related records in another
	 *            database identified by part 2.
	 */
	public DatabaseRelationIterator(Database database, Transaction transaction, long searchId) {
		this.db = database;
		this.txn = transaction;
		this.searchId = searchId;
		
		keyBinding = new StoreableTupleBinding<LongLongIndexElement>(LongLongIndexElement.class);
		
		initialized = false;
		nextRecordAvailable = false;
		
		keyEntry = new DatabaseEntry();
		dataEntry = new DatabaseEntry();
	}
	
	
	private void initialize() {
		try {
			LongLongIndexElement startKey;
			
			// We want to start retrieving records from the first record with a
			// part 1 of searchId. Therefore part2 must be the minimum value.
			// HACK: Note that this should be using Long.MIN_VALUE but the
			// internal implementation is currently using an integer.
			startKey = new LongLongIndexElement(searchId, Integer.MIN_VALUE);
			keyBinding.objectToEntry(startKey, keyEntry);
			
			cursor = db.openCursor(txn, null);
			cursorLive = true;
			if (OperationStatus.SUCCESS.equals(cursor.getSearchKeyRange(keyEntry, dataEntry, null))) {
				extractNextRecord();
			} else {
				cursorLive = false;
			}
			
		} catch (DatabaseException e) {
			throw new OsmosisRuntimeException("Unable to open database cursor.", e);
		}
		
		initialized = true;
	}
	
	
	/**
	 * Extracts the next record from the cursor and checks if the end of the
	 * range has been reached.
	 */
	private void extractNextRecord() {
		LongLongIndexElement indexElement;
		
		indexElement = (LongLongIndexElement) keyBinding.entryToObject(keyEntry);
		
		if (indexElement.getPart1() == searchId) {
			nextRecord = indexElement.getPart2();
			nextRecordAvailable = true;
		} else {
			cursorLive = false;
		}
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
		
		if (cursorLive && !nextRecordAvailable) {
			try {
				if (OperationStatus.SUCCESS.equals(cursor.getNext(keyEntry, dataEntry, null))) {
					extractNextRecord();
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
	public Long next() {
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
