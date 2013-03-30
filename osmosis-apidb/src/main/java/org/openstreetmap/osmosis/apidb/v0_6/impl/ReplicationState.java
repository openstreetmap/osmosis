// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.apidb.v0_6.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;


/**
 * Contains the state to be remembered between replication invocations. This state ensures that no
 * data is missed during replication, and none is repeated.
 */
public class ReplicationState extends org.openstreetmap.osmosis.replication.common.ReplicationState {
	private long txnMax;
	private long txnMaxQueried;
	private List<Long> txnActive;
	private List<Long> txnReady;


	/**
	 * Creates a new instance with all values set to defaults.
	 */
	public ReplicationState() {
		super();
		this.txnMax = 0;
		this.txnMaxQueried = 0;
		this.txnActive = new ArrayList<Long>();
		this.txnReady = new ArrayList<Long>();
	}


	/**
	 * Creates a new instance.
	 * 
	 * @param txnMax
	 *            The maximum transaction id in the database.
	 * @param txnMaxQueried
	 *            The maximum transaction id currently replicated from the database.
	 * @param txnActive
	 *            The currently active transaction ids.
	 * @param txnReady
	 *            The previously active transaction ids that can now be queried.
	 * @param timestamp
	 *            The maximum timestamp of data currently read from the database.
	 * @param sequenceNumber
	 *            The replication sequence number.
	 */
	public ReplicationState(long txnMax, long txnMaxQueried, List<Long> txnActive, List<Long> txnReady,
			Date timestamp, long sequenceNumber) {
		super(timestamp, sequenceNumber);
		this.txnMax = txnMax;
		this.txnMaxQueried = txnMaxQueried;
		this.txnActive = new ArrayList<Long>(txnActive);
		this.txnReady = new ArrayList<Long>(txnReady);
	}
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param properties
	 *            The properties to load state from.
	 */
	public ReplicationState(Map<String, String> properties) {
		load(properties);
	}
	
	
	/**
	 * Loads all state from the provided properties object.
	 * 
	 * @param properties
	 *            The properties to be read.
	 */
	public void load(Map<String, String> properties) {
		super.load(properties);
		txnMax = Long.parseLong(properties.get("txnMax"));
		txnMaxQueried = Long.parseLong(properties.get("txnMaxQueried"));
		txnActive = fromString(properties.get("txnActiveList"));
		txnReady = fromString(properties.get("txnReadyList"));
	}


	@Override
	public void store(Map<String, String> properties) {
		super.store(properties);
		properties.put("txnMax", Long.toString(txnMax));
		properties.put("txnMaxQueried", Long.toString(txnMaxQueried));
		properties.put("txnActiveList", toString(txnActive));
		properties.put("txnReadyList", toString(txnReady));
	}
	
	
	@Override
	public Map<String, String> store() {
		Map<String, String> properties = new HashMap<String, String>();
		store(properties);
		return properties;
	}


	private String toString(List<Long> values) {
		StringBuilder buffer;
		
		buffer = new StringBuilder();
		for (long value : values) {
			if (buffer.length() > 0) {
				buffer.append(',');
			}
			buffer.append(value);
		}
		
		return buffer.toString();
	}
	
	
	private List<Long> fromString(String values) {
		StringTokenizer tokens;
		List<Long> result;
		
		tokens = new StringTokenizer(values, ",");
		
		result = new ArrayList<Long>();
		while (tokens.hasMoreTokens()) {
			result.add(Long.parseLong(tokens.nextToken()));
		}
		
		return result;
	}


	/**
	 * Gets the maximum transaction id in the database.
	 * 
	 * @return The transaction id.
	 */
	public long getTxnMax() {
		return txnMax;
	}
	

	/**
	 * Sets the maximum transaction id in the database.
	 * 
	 * @param txnMax
	 *            The transaction id.
	 */
	public void setTxnMax(long txnMax) {
		this.txnMax = txnMax;
	}


	/**
	 * Gets the maximum transaction id currently replicated from the database.
	 * 
	 * @return The transaction id.
	 */
	public long getTxnMaxQueried() {
		return txnMaxQueried;
	}


	/**
	 * Sets the maximum transaction id currently replicated from the database.
	 * 
	 * @param txnMaxQueried
	 *            The transaction id.
	 */
	public void setTxnMaxQueried(long txnMaxQueried) {
		this.txnMaxQueried = txnMaxQueried;
	}


	/**
	 * Gets the currently active transaction ids. These cannot be replicated until they have
	 * committed.
	 * 
	 * @return The list of transaction ids.
	 */
	public List<Long> getTxnActive() {
		return txnActive;
	}


	/**
	 * Gets the previously active transaction ids that can now be queried.
	 * 
	 * @return The list of transaction ids.
	 */
	public List<Long> getTxnReady() {
		return txnReady;
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object obj) {
		boolean result;
		
		if (obj instanceof ReplicationState) {
			ReplicationState compareState = (ReplicationState) obj;
			
			if (super.equals(obj)
					&& txnMax == compareState.txnMax
					&& txnMaxQueried == compareState.txnMaxQueried
					&& txnActive.equals(compareState.txnActive)
					&& txnReady.equals(compareState.txnReady)) {
				result = true;
			} else {
				result = false;
			}
		} else {
			result = false;
		}
		
		return result;
	}


	@Override
	public int hashCode() {
		return super.hashCode() + (int) txnMax + (int) txnMaxQueried;
	}


	@Override
	public String toString() {
		return "ReplicationState(txnMax=" + txnMax + ", txnMaxQueried=" + txnMaxQueried + ", txnActive=" + txnActive
				+ ", txnReady=" + txnReady + ", timestamp=" + getTimestamp() + ", sequenceNumber="
				+ getSequenceNumber() + ")";
	}
}
