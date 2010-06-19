// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.apidb.v0_6.impl;

import java.util.ArrayList;
import java.util.List;

import org.openstreetmap.osmosis.core.container.v0_6.ChangeContainer;
import org.openstreetmap.osmosis.core.lifecycle.ReleasableIterator;
import org.openstreetmap.osmosis.core.store.EmptyIterator;


/**
 * A mocked replication source capturing provided predicates for later analysis and returning empty
 * data sets on each call.
 */
public class MockReplicationSource implements ReplicationSource {

	private List<ReplicationQueryPredicates> predicatesList = new ArrayList<ReplicationQueryPredicates>();


	/**
	 * Gets the query predicates passed to this mock during execution.
	 * 
	 * @return The query predicates.
	 */
	public List<ReplicationQueryPredicates> getPredicatesList() {
		return predicatesList;
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public ReleasableIterator<ChangeContainer> getHistory(ReplicationQueryPredicates predicates) {
		predicatesList.add(predicates);
		
		return new EmptyIterator<ChangeContainer>();
	}
}
