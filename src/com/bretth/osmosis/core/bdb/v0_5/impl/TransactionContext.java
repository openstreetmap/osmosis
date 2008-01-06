// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package com.bretth.osmosis.core.bdb.v0_5.impl;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.bretth.osmosis.core.OsmosisRuntimeException;
import com.bretth.osmosis.core.store.Completable;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Transaction;


/**
 * Maintains the state associated with a single transaction interaction with a
 * dataset schema allowing operations to be performed on the schema.
 * 
 * @author Brett Henderson
 */
public class TransactionContext implements Completable {
	private static Logger log = Logger.getLogger(TransactionContext.class.getName());
	
	private Transaction txn;
	private Database dbNode;
	private Database dbWay;
	private Database dbRelation;
	private boolean commited;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param transaction
	 *            The transaction to perform all operations under.
	 * @param dbNode
	 *            The node database.
	 * @param dbWay
	 *            The way database.
	 * @param dbRelation
	 *            The relation database.
	 */
	public TransactionContext(Transaction transaction, Database dbNode, Database dbWay, Database dbRelation) {
		this.txn = transaction;
		this.dbNode = dbNode;
		this.dbWay = dbWay;
		this.dbRelation = dbRelation;
		
		commited = false;
	}
	
	
	/**
	 * Returns the node dao associated with this transaction.
	 * 
	 * @return The node dao.
	 */
	public NodeDao getNodeDao() {
		return new NodeDao(txn, dbNode);
	}
	
	
	/**
	 * Returns the way dao associated with this transaction.
	 * 
	 * @return The way dao.
	 */
	public WayDao getWayDao() {
		return new WayDao(txn, dbWay);
	}
	
	
	/**
	 * Returns the relation dao associated with this transaction.
	 * 
	 * @return The relation dao.
	 */
	public RelationDao getRelationDao() {
		return new RelationDao(txn, dbRelation);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void complete() {
		try {
			txn.commit();
			
		} catch (DatabaseException e) {
			throw new OsmosisRuntimeException("Unable to commit the transaction.", e);
		}
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void release() {
		if (!commited) {
			try {
				txn.abort();
			} catch (DatabaseException e) {
				// Don't rethrow, just log the exception.
				log.log(Level.SEVERE, "Unable to abort the transaction", e);
			}
		}
	}
}
