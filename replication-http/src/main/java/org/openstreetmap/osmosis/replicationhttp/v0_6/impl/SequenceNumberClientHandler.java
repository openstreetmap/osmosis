// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.replicationhttp.v0_6.impl;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.util.CharsetUtil;


/**
 * Netty handler for receiving replication sequence numbers and notifying
 * listeners.
 * 
 * @author Brett Henderson
 */
public class SequenceNumberClientHandler extends SequenceClientHandler {

	private SequenceNumberClientControl control;


	/**
	 * Creates a new instance.
	 * 
	 * @param control
	 *            Provides the Netty handlers with access to the controller.
	 */
	public SequenceNumberClientHandler(SequenceNumberClientControl control) {
		super(control);

		this.control = control;
	}


	@Override
	protected void processMessageData(ChannelBuffer buffer) {
		// The readable data is the sequence number in string form.
		String sequenceNumberString = buffer.toString(CharsetUtil.UTF_8);
		long sequenceNumber = Long.parseLong(sequenceNumberString);

		// Send the new sequence number notification.
		control.notifySequenceNumber(sequenceNumber);
	}
}
