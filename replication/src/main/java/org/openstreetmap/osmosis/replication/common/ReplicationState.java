// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.replication.common;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.openstreetmap.osmosis.core.time.DateFormatter;
import org.openstreetmap.osmosis.core.time.DateParser;


/**
 * Contains the state to be remembered between replication invocations. This
 * state ensures that no data is missed during replication, and ensures that
 * none is repeated except after certain failure situations.
 */
public class ReplicationState {
	/**
	 * The key used when passing an instance through the pipeline as metadata.
	 */
	public static final String META_DATA_KEY = "replication.state";
	
	
	private Date timestamp;
	private long sequenceNumber;


	/**
	 * Creates a new instance with all values set to defaults.
	 */
	public ReplicationState() {
		this.timestamp = new Date(0);
		this.sequenceNumber = 0;
	}


	/**
	 * Creates a new instance.
	 * 
	 * @param timestamp
	 *            The maximum timestamp of data currently read from the database.
	 * @param sequenceNumber
	 *            The replication sequence number.
	 */
	public ReplicationState(Date timestamp, long sequenceNumber) {
		this.timestamp = timestamp;
		this.sequenceNumber = sequenceNumber;
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
		timestamp = new DateParser().parse(properties.get("timestamp"));
		sequenceNumber = Long.parseLong(properties.get("sequenceNumber"));
	}


	/**
	 * Writes all state into the provided properties object.
	 * 
	 * @param properties
	 *            The properties to be updated.
	 */
	public void store(Map<String, String> properties) {
		properties.put("timestamp", new DateFormatter().format(timestamp));
		properties.put("sequenceNumber", Long.toString(sequenceNumber));
	}


	/**
	 * Writes all state into a new properties object.
	 * 
	 * @return The properties.
	 */
	public Map<String, String> store() {
		Map<String, String> properties = new HashMap<String, String>();
		store(properties);
		return properties;
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
			
			if (timestamp.equals(compareState.timestamp)
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
		return (int) sequenceNumber + (int) timestamp.getTime();
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return "ReplicationState(timestamp=" + timestamp + ", sequenceNumber=" + sequenceNumber + ")";
	}
}
