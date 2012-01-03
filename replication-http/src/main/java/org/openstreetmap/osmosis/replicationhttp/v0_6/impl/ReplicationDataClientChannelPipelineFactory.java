// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.replicationhttp.v0_6.impl;

import org.openstreetmap.osmosis.core.task.v0_6.ChangeSink;


/**
 * Builds Netty channel pipelines for new connections to servers.
 * 
 * @author Brett Henderson
 */
public class ReplicationDataClientChannelPipelineFactory extends SequenceClientChannelPipelineFactory {

	private ChangeSink changeSink;


	/**
	 * Creates a new instance.
	 * 
	 * @param control
	 *            Provides the Netty handlers with access to the controller.
	 * @param changeSink
	 *            The destination for the replication data.
	 */
	public ReplicationDataClientChannelPipelineFactory(SequenceClientControl control,
			ChangeSink changeSink) {
		super(control);

		this.changeSink = changeSink;
	}


	@Override
	protected SequenceClientHandler createHandler(SequenceClientControl control) {
		return new ReplicationDataClientHandler(control, changeSink);
	}
}
