// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.apidb.v0_6.impl;


/**
 * Obtains transaction snapshots used for replication.
 */
public interface TransactionManager {
	/**
	 * Obtains the current database snapshot.
	 * 
	 * @return The transaction snapshot.
	 */
	TransactionSnapshot getTransactionSnapshot();
	
	
	/**
	 * Executes the specified object within a transaction.
	 * 
	 * @param target
	 *            The object containing the logic to execute.
	 */
	void executeWithinTransaction(Runnable target);
}
