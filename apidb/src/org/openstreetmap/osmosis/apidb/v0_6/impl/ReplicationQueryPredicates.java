// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.apidb.v0_6.impl;

import java.util.ArrayList;
import java.util.List;


/**
 * Contains the parameters required to perform a single replication from the database.
 */
public class ReplicationQueryPredicates {
	private long bottomTransactionId;
	private long topTransactionId;
	private List<Long> readyList;
	private List<Long> activeList;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param bottomTransactionId
	 *            The transaction id to begin querying from. This will be included in the query.
	 * @param topTransactionId
	 *            The transaction id to stop querying at. This will not be included in the query.
	 */
	public ReplicationQueryPredicates(long bottomTransactionId, long topTransactionId) {
		this.bottomTransactionId = bottomTransactionId;
		this.topTransactionId = topTransactionId;
		
		readyList = new ArrayList<Long>();
		activeList = new ArrayList<Long>();
	}
	
	
	/**
	 * Gets the transaction id to begin querying from. This will be included in the query.
	 * 
	 * @return The transaction id.
	 */
	public long getBottomTransactionId() {
		return bottomTransactionId;
	}
	
	
	/**
	 * Gets the transaction id to stop querying at.  This will not be included in the query.
	 * 
	 * @return The transaction id.
	 */
	public long getTopTransactionId() {
		return topTransactionId;
	}


	/**
	 * Gets the transaction ready list. These will be included in the query in addition to those the ids
	 * in the range defined by the bottom and top transaction id.
	 * 
	 * @return The transaction id list.
	 */
	public List<Long> getReadyList() {
		return readyList;
	}
	
	
	/**
	 * Gets the transaction active list. These will be excluded from the query results due to them
	 * being active transactions at the point when the snapshot was taken.
	 * 
	 * @return The transaction id list.
	 */
	public List<Long> getActiveList() {
		return activeList;
	}
}
