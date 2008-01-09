// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package com.bretth.osmosis.core.bdb.v0_5.impl;

import com.bretth.osmosis.core.OsmosisRuntimeException;
import com.bretth.osmosis.core.bdb.common.LongLongIndexElement;
import com.bretth.osmosis.core.bdb.common.NoSuchDatabaseEntryException;
import com.bretth.osmosis.core.bdb.common.StoreableTupleBinding;
import com.bretth.osmosis.core.domain.v0_5.Way;
import com.bretth.osmosis.core.domain.v0_5.WayNode;
import com.bretth.osmosis.core.store.ReleasableIterator;
import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.OperationStatus;
import com.sleepycat.je.Transaction;


/**
 * Performs all way-specific db operations.
 * 
 * @author Brett Henderson
 */
public class WayDao {
	
	private Transaction txn;
	private Database dbWay;
	private Database dbNodeWay;
	private TupleBinding idBinding;
	private StoreableTupleBinding<Way> wayBinding;
	private StoreableTupleBinding<LongLongIndexElement> longLongIndexBinding;
	private DatabaseEntry keyEntry;
	private DatabaseEntry dataEntry;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param transaction
	 *            The active transaction.
	 * @param dbWay
	 *            The way database.
	 * @param dbNodeWay
	 *            The node-way database.
	 */
	public WayDao(Transaction transaction, Database dbWay, Database dbNodeWay) {
		this.txn = transaction;
		this.dbWay = dbWay;
		this.dbNodeWay = dbNodeWay;
		
		idBinding = TupleBinding.getPrimitiveBinding(Long.class);
		wayBinding = new StoreableTupleBinding<Way>(Way.class);
		longLongIndexBinding = new StoreableTupleBinding<LongLongIndexElement>(LongLongIndexElement.class);
		keyEntry = new DatabaseEntry();
		dataEntry = new DatabaseEntry();
	}
	
	
	/**
	 * Stores the way in the way database.
	 * 
	 * @param way 
	 *            The way to be stored.
	 */
	public void putWay(Way way) {
		// Write the way object to the way database.
		idBinding.objectToEntry(way.getId(), keyEntry);
		wayBinding.objectToEntry(way, dataEntry);
		
		try {
			dbWay.put(txn, keyEntry, dataEntry);
		} catch (DatabaseException e) {
			throw new OsmosisRuntimeException("Unable to write way " + way.getId() + ".", e);
		}
		
		// Write a node to way index records for the way.
		for (WayNode wayNode : way.getWayNodeList()) {
			longLongIndexBinding.objectToEntry(new LongLongIndexElement(wayNode.getNodeId(), way.getId()), keyEntry);
			dataEntry.setSize(0);
			
			try {
				dbNodeWay.put(txn, keyEntry, dataEntry);
			} catch (DatabaseException e) {
				throw new OsmosisRuntimeException("Unable to write way node index for way " + way.getId() + ".", e);
			}
		}
	}
	
	
	/**
	 * Gets the specified way from the way database.
	 * 
	 * @param wayId
	 *            The id of the way to be retrieved.
	 * @return The requested way.
	 */
	public Way getWay(long wayId) {
		idBinding.objectToEntry(wayId, keyEntry);
		
		try {
			if (!OperationStatus.SUCCESS.equals(dbWay.get(txn, keyEntry, dataEntry, null))) {
				throw new NoSuchDatabaseEntryException("Way " + wayId + " does not exist in the database.");
			}
		} catch (DatabaseException e) {
			throw new OsmosisRuntimeException("Unable to retrieve way " + wayId + " from the database.", e);
		}
		
		return (Way) wayBinding.entryToObject(dataEntry);
	}
	
	
	/**
	 * Provides access to all ways in the database. The iterator must be
	 * released after use.
	 * 
	 * @return An iterator pointing at the first way.
	 */
	public ReleasableIterator<Way> iterate() {
		return new DatabaseIterator<Way>(dbWay, txn, wayBinding);
	}
	
	
	/**
	 * Returns an iterator for the ids of all ways containing the specified
	 * node.
	 * 
	 * @param nodeId
	 *            The id of the node to search on.
	 * @return The ids of the matching ways.
	 */
	public ReleasableIterator<Long> getWayIdsOwningNode(long nodeId) {
		return new DatabaseRelationIterator(dbNodeWay, txn, nodeId);
	}
}
