// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package com.bretth.osmosis.core.bdb.v0_5.impl;

import com.bretth.osmosis.core.OsmosisRuntimeException;
import com.bretth.osmosis.core.bdb.common.StoreableTupleBinding;
import com.bretth.osmosis.core.domain.v0_5.Relation;
import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Transaction;


/**
 * Performs all relation-specific db operations.
 * 
 * @author Brett Henderson
 */
public class RelationDao {
	private Transaction txn;
	private Database dbRelation;
	private TupleBinding idBinding;
	private StoreableTupleBinding<Relation> relationBinding;
	private DatabaseEntry keyEntry;
	private DatabaseEntry dataEntry;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param transaction
	 *            The active transaction.
	 * @param dbRelation
	 *            The Relationtion database.
	 */
	public RelationDao(Transaction transaction, Database dbRelation) {
		this.txn = transaction;
		this.dbRelation = dbRelation;
		
		idBinding = TupleBinding.getPrimitiveBinding(Long.class);
		relationBinding = new StoreableTupleBinding<Relation>();
		keyEntry = new DatabaseEntry();
		dataEntry = new DatabaseEntry();
	}
	
	
	/**
	 * Writes the relation to the relation database.
	 * 
	 * @param relation
	 *            The relation to be written.
	 */
	public void putRelation(Relation relation) {
		idBinding.objectToEntry(relation.getId(), keyEntry);
		relationBinding.objectToEntry(relation, dataEntry);
		
		try {
			dbRelation.put(txn, keyEntry, dataEntry);
		} catch (DatabaseException e) {
			throw new OsmosisRuntimeException("Unable to write relation " + relation.getId() + ".", e);
		}
	}
}
