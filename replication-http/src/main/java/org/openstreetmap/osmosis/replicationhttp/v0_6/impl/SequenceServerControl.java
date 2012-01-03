// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.replicationhttp.v0_6.impl;

import org.jboss.netty.channel.Channel;


/**
 * This interface provides Netty handlers executing in worker threads with
 * access to sequence server control methods.
 * 
 * @author Brett Henderson
 */
public interface SequenceServerControl {

	/**
	 * Allows a Netty handler to request the latest sequence number from the
	 * controller.
	 * 
	 * @return The latest sequence number.
	 */
	long getLatestSequenceNumber();


	/**
	 * Allows a Netty handler to notify the controller that the channel is ready
	 * for more data. If the controller has new sequence information available
	 * it will send it, otherwise it will add the channel to the waiting list.
	 * 
	 * @param channel
	 *            The client channel.
	 * @param lastSequenceNumber
	 *            The last sequence number received by the channel.
	 * @param follow
	 *            If true, the channel will be held open and updated sequences
	 *            sent as they arrive.
	 */
	void determineNextChannelAction(Channel channel, long lastSequenceNumber, boolean follow);


	/**
	 * Allows a Netty handler to register a channel with the main controller.
	 * This allows the controller to close the channel when the server shuts
	 * down.
	 * 
	 * @param channel
	 *            The channel to be registered.
	 */
	void registerChannel(Channel channel);
}
