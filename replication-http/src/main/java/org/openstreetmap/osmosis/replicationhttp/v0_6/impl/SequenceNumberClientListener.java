// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.replicationhttp.v0_6.impl;

/**
 * Used by SequenceNumberClientHandler to notify a listener about received
 * sequence numbers.
 * 
 * @author Brett Henderson
 */
public interface SequenceNumberClientListener {

	/**
	 * Allows a Netty handler to notify when a new sequence number has been
	 * received.
	 * 
	 * @param sequenceNumber
	 *            The received sequence number.
	 */
	void notifySequenceNumber(long sequenceNumber);
}
