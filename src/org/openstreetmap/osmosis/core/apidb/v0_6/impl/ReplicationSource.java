// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.apidb.v0_6.impl;

import java.util.Date;
import java.util.List;

import org.openstreetmap.osmosis.core.container.v0_6.ChangeContainer;
import org.openstreetmap.osmosis.core.lifecycle.ReleasableIterator;


/**
 * Provides {@link Replicator} with change streams that it consumes.
 */
public interface ReplicationSource {
	/**
	 * Retrieves the changes that have were made by a set of transactions.
	 * 
	 * @param baseTimestamp
	 *            The timestamp to constrain the query by. This timestamp is included for
	 *            performance reasons and limits the amount of data searched for the transaction
	 *            ids.
	 * @param txnList
	 *            The set of transactions to query for.
	 * @return An iterator pointing at the identified records.
	 */
	ReleasableIterator<ChangeContainer> getHistory(Date baseTimestamp, List<Long> txnList);
}
