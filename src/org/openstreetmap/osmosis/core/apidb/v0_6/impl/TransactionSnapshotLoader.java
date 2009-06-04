// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.apidb.v0_6.impl;


/**
 * Obtains transaction snapshots used for replication.
 */
public interface TransactionSnapshotLoader {
	/**
	 * Obtains the current database snapshot.
	 * 
	 * @return The transaction snapshot.
	 */
	TransactionSnapshot getTransactionSnapshot();
}
