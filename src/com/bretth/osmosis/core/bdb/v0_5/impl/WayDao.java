// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package com.bretth.osmosis.core.bdb.v0_5.impl;

import com.bretth.osmosis.core.OsmosisRuntimeException;
import com.bretth.osmosis.core.bdb.common.StoreableTupleBinding;
import com.bretth.osmosis.core.domain.v0_5.Way;
import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Transaction;


/**
 * Performs all way-specific db operations.
 * 
 * @author Brett Henderson
 */
public class WayDao {
	private Transaction txn;
	private Database dbWay;
	private TupleBinding idBinding;
	private StoreableTupleBinding<Way> wayBinding;
	private DatabaseEntry keyEntry;
	private DatabaseEntry dataEntry;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param transaction
	 *            The active transaction.
	 * @param dbWay
	 *            The way database.
	 */
	public WayDao(Transaction transaction, Database dbWay) {
		this.txn = transaction;
		this.dbWay = dbWay;
		
		idBinding = TupleBinding.getPrimitiveBinding(Long.class);
		wayBinding = new StoreableTupleBinding<Way>();
		keyEntry = new DatabaseEntry();
		dataEntry = new DatabaseEntry();
	}
	
	/**
	 * Writes the way to the way database.
	 * 
	 * @param way 
	 *            The way to be written.
	 */
	public void putWay(Way way) {
		idBinding.objectToEntry(way.getId(), keyEntry);
		wayBinding.objectToEntry(way, dataEntry);
		
		try {
			dbWay.put(txn, keyEntry, dataEntry);
		} catch (DatabaseException e) {
			throw new OsmosisRuntimeException("Unable to write way " + way.getId() + ".", e);
		}
	}
}
