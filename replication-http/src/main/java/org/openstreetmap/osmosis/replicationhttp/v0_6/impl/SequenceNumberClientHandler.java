// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.replicationhttp.v0_6.impl;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.util.CharsetUtil;


/**
 * Netty handler for receiving replication sequence numbers and notifying
 * listeners.
 * 
 * @author Brett Henderson
 */
public class SequenceNumberClientHandler extends SequenceClientHandler {

	private static final Logger LOG = Logger.getLogger(SequenceNumberClientHandler.class.getName());

	private SequenceNumberClientListener sequenceNumberListener;


	/**
	 * Creates a new instance.
	 * 
	 * @param control
	 *            Provides the Netty handlers with access to the controller.
	 * @param sequenceNumberListener
	 *            This will be notified when new sequence numbers are received.
	 * @param serverHost
	 *            The name of the host system running the sequence server.
	 */
	public SequenceNumberClientHandler(SequenceClientControl control,
			SequenceNumberClientListener sequenceNumberListener, String serverHost) {
		super(control, serverHost);

		this.sequenceNumberListener = sequenceNumberListener;
	}


	@Override
	protected String getRequestUri() {
		return "/sequenceNumber/current/tail";
	}


	@Override
	protected void processMessageData(ChannelBuffer buffer) {
		// The readable data is the sequence number in string form.
		String sequenceNumberString = buffer.toString(CharsetUtil.UTF_8);
		long sequenceNumber = Long.parseLong(sequenceNumberString);
		if (LOG.isLoggable(Level.FINEST)) {
			LOG.finest("Received sequence number " + sequenceNumber);
		}

		// Send the new sequence number notification.
		sequenceNumberListener.notifySequenceNumber(sequenceNumber);
	}
}
