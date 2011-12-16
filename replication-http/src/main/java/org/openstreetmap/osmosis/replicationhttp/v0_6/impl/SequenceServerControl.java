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
	 * Allows a Netty handler to request the controller to initialize sending
	 * sequence numbers to a channel. If follow is specified the controller will
	 * continue sending updated sequence values for the life of the server, or
	 * until the client disconnects. Otherwise only a single sequence will be
	 * sent.
	 * 
	 * @param channel
	 *            The channel to send sequence numbers to.
	 * @param follow
	 *            If true, the channel will be held open and updated sequences
	 *            sent as they are arrive.
	 */
	void sendSequenceNumber(Channel channel, boolean follow);


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
