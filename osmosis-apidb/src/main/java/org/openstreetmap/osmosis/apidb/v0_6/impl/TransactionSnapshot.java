// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.apidb.v0_6.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Represents the data associated with a database transaction snapshot providing information about
 * currently in-flight transactions.
 */
public class TransactionSnapshot {
	private long xMin;
	private long xMax;
	private List<Long> xIpList;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param snapshotString
	 *            The snapshot string in format "xMin:xMax:inflight1,inflight2,...".
	 */
	public TransactionSnapshot(String snapshotString) {
		StringTokenizer tokenizer;
		
		tokenizer = new StringTokenizer(snapshotString, ":");
		
		xMin = Long.parseLong(tokenizer.nextToken());
		xMax = Long.parseLong(tokenizer.nextToken());
		
		xIpList = new ArrayList<Long>();
		if (tokenizer.hasMoreTokens()) {
			tokenizer = new StringTokenizer(tokenizer.nextToken(), ",");
			while (tokenizer.hasMoreTokens()) {
				xIpList.add(Long.parseLong(tokenizer.nextToken()));
			}
		}
	}


	/**
	 * Gets the earliest still active transaction.
	 * 
	 * @return The transaction id.
	 */
	public long getXMin() {
		return xMin;
	}


	/**
	 * Gets the next transaction to be created.
	 * 
	 * @return The transaction id.
	 */
	public long getXMax() {
		return xMax;
	}


	/**
	 * Gets the list of active transactions.
	 * 
	 * @return The transaction ids.
	 */
	public List<Long> getXIpList() {
		return xIpList;
	}
}
