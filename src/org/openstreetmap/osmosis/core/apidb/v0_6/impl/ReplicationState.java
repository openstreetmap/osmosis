// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.apidb.v0_6.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;

import org.openstreetmap.osmosis.core.store.StoreClassRegister;
import org.openstreetmap.osmosis.core.store.StoreReader;
import org.openstreetmap.osmosis.core.store.StoreWriter;
import org.openstreetmap.osmosis.core.store.Storeable;
import org.openstreetmap.osmosis.core.xml.common.DateFormatter;
import org.openstreetmap.osmosis.core.xml.common.DateParser;


/**
 * Contains the state to be remembered between replication invocations. This state ensures that no
 * data is missed during replication, and none is repeated.
 */
public class ReplicationState implements Storeable {
	private long txnMax;
	private long txnMaxQueried;
	private List<Long> txnActive;
	private List<Long> txnReady;
	private Date timestamp;
	private long sequenceNumber;


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
		this.txnMax = txnMax;
		this.txnMaxQueried = txnMaxQueried;
		this.txnActive = txnActive;
		this.txnReady = txnReady;
		this.timestamp = timestamp;
		this.sequenceNumber = sequenceNumber;
	}
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param reader
	 *            The store to read state from.
	 * @param scr
	 *            Maintains the mapping between classes and their identifiers
	 *            within the store.
	 */
	public ReplicationState(StoreReader reader, StoreClassRegister scr) {
		int txnActiveCount;
		int txnReadyCount;
		
		txnMax = reader.readLong();
		txnMaxQueried = reader.readLong();
		
		txnActiveCount = reader.readInteger();
		txnActive = new ArrayList<Long>();
		for (int i = 0; i < txnActiveCount; i++) {
			txnActive.add(reader.readLong());
		}
		
		txnReadyCount = reader.readInteger();
		txnReady = new ArrayList<Long>();
		for (int i = 0; i < txnReadyCount; i++) {
			txnReady.add(reader.readLong());
		}
		
		timestamp = new Date(reader.readLong());
		sequenceNumber = reader.readLong();
	}
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param properties
	 *            The properties to load state from.
	 */
	public ReplicationState(Properties properties) {
		txnMax = Long.parseLong(properties.getProperty("txnMax"));
		txnMaxQueried = Long.parseLong(properties.getProperty("txnMaxQueried"));
		txnActive = fromString(properties.getProperty("txnActiveList"));
		txnReady = fromString(properties.getProperty("txnReadyList"));
		timestamp = new DateParser().parse(properties.getProperty("timestamp"));
		sequenceNumber = Long.parseLong(properties.getProperty("sequenceNumber"));
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public void store(StoreWriter writer, StoreClassRegister storeClassRegister) {
		writer.writeLong(txnMax);
		writer.writeLong(txnMaxQueried);
		
		writer.writeInteger(txnActive.size());
		for (Long value : txnActive) {
			writer.writeLong(value);
		}
		
		writer.writeInteger(txnReady.size());
		for (Long value : txnReady) {
			writer.writeLong(value);
		}
		
		writer.writeLong(timestamp.getTime());
		writer.writeLong(sequenceNumber);
	}


	/**
	 * Writes all state into the provided properties object.
	 * 
	 * @param properties
	 *            The properties to be updated.
	 */
	public void store(Properties properties) {
		properties.setProperty("txnMax", Long.toString(txnMax));
		properties.setProperty("txnMaxQueried", Long.toString(txnMaxQueried));
		properties.setProperty("txnActiveList", toString(txnActive));
		properties.setProperty("txnReadyList", toString(txnReady));
		properties.setProperty("timestamp", new DateFormatter().format(timestamp));
		properties.setProperty("sequenceNumber", Long.toString(sequenceNumber));
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
	 * Gets the maximum timestamp of data currently read from the database.
	 * 
	 * @return The timestamp.
	 */
	public Date getTimestamp() {
		return timestamp;
	}


	/**
	 * Sets the maximum timestamp of data currently read from the database.
	 * 
	 * @param timestamp
	 *            The timestamp.
	 */
	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}
	
	
	/**
	 * Gets the replication sequence number.
	 * 
	 * @return The sequence number.
	 */
	public long getSequenceNumber() {
		return sequenceNumber;
	}


	/**
	 * Sets the replication sequence number.
	 * 
	 * @param sequenceNumber
	 *            The sequence number.
	 */
	public void setSequenceNumber(long sequenceNumber) {
		this.sequenceNumber = sequenceNumber;
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object obj) {
		boolean result;
		
		if (obj instanceof ReplicationState) {
			ReplicationState compareState = (ReplicationState) obj;
			
			if (txnMax == compareState.txnMax
					&& txnMaxQueried == compareState.txnMaxQueried
					&& txnActive.equals(compareState.txnActive)
					&& txnReady.equals(compareState.txnReady)
					&& timestamp.equals(compareState.timestamp)
					&& sequenceNumber == compareState.sequenceNumber) {
				result = true;
			} else {
				result = false;
			}
		} else {
			result = false;
		}
		
		return result;
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		return (int) sequenceNumber + (int) txnMax + (int) txnMaxQueried + (int) timestamp.getTime();
	}
}
