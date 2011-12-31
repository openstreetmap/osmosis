// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.replicationhttp.v0_6.impl;

/**
 * This interface provides Netty handlers executing in worker threads with
 * access to sequence number client control methods.
 * 
 * @author Brett Henderson
 */
public interface SequenceNumberClientControl extends SequenceClientControl {

	/**
	 * Allows a Netty handler to tell the controller that a new sequence number
	 * has been received.
	 * 
	 * @param sequenceNumber
	 *            The received sequence number.
	 */
	void notifySequenceNumber(long sequenceNumber);
}
