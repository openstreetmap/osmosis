// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.apidb.v0_6.impl;

import org.openstreetmap.osmosis.core.container.v0_6.ChangeContainer;
import org.openstreetmap.osmosis.core.lifecycle.ReleasableIterator;


/**
 * Provides {@link Replicator} with change streams that it consumes.
 */
public interface ReplicationSource {
	/**
	 * Retrieves the changes that have were made by a set of transactions.
	 * 
	 * @param predicates
	 *            Contains the predicates defining the transactions to be queried.
	 * @return An iterator pointing at the identified records.
	 */
	ReleasableIterator<ChangeContainer> getHistory(ReplicationQueryPredicates predicates);
}
