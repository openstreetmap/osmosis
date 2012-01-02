// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.replicationhttp.v0_6;

import java.io.File;
import java.net.InetAddress;
import java.net.InetSocketAddress;

import org.openstreetmap.osmosis.core.task.v0_6.ChangeSink;
import org.openstreetmap.osmosis.core.task.v0_6.RunnableChangeSource;
import org.openstreetmap.osmosis.replication.common.ReplicationState;
import org.openstreetmap.osmosis.replicationhttp.v0_6.impl.ReplicationDataClientChannelPipelineFactory;
import org.openstreetmap.osmosis.replicationhttp.v0_6.impl.ReplicationDataClientControl;
import org.openstreetmap.osmosis.replicationhttp.v0_6.impl.SequenceClient;


/**
 * This task connects to a replication data server to obtain replication data,
 * then sends the replication data to the sink. This requires the change sink to
 * support the replication extensions allowing state persistence and multiple
 * invocations.
 * 
 * @author Brett Henderson
 */
public class ReplicationDataClient implements RunnableChangeSource {

	private ChangeSink changeSink;
	private InetSocketAddress serverAddress;


	/**
	 * Creates a new instance.
	 * 
	 * @param serverAddress
	 *            The server to connect to.
	 */
	public ReplicationDataClient(InetSocketAddress serverAddress) {
		this.serverAddress = serverAddress;
	}


	@Override
	public void setChangeSink(ChangeSink changeSink) {
		this.changeSink = changeSink;
	}


	@Override
	public void run() {
		try {
			new ReplicationDataClientControl() {

				@Override
				public void channelClosed() {
					// TODO Auto-generated method stub

				}


				@Override
				public void sendReplicationData(ReplicationState state, File replicationData) {
					// TODO Auto-generated method stub

				}
			};
			new SequenceClient<ReplicationDataClientControl>(serverAddress,
					new ReplicationDataClientChannelPipelineFactory(control));

		} finally {
			changeSink.release();
		}
	}
}
