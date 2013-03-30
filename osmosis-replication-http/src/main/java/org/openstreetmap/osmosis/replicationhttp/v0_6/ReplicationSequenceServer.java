// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.replicationhttp.v0_6;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.openstreetmap.osmosis.core.OsmosisRuntimeException;
import org.openstreetmap.osmosis.core.container.v0_6.ChangeContainer;
import org.openstreetmap.osmosis.core.task.v0_6.ChangeSink;
import org.openstreetmap.osmosis.core.task.v0_6.ChangeSinkChangeSource;
import org.openstreetmap.osmosis.replication.common.ReplicationState;
import org.openstreetmap.osmosis.replicationhttp.v0_6.impl.SequenceNumberServerChannelPipelineFactory;
import org.openstreetmap.osmosis.replicationhttp.v0_6.impl.SequenceServer;


/**
 * This task creates a HTTP server that sends updated replication sequence
 * numbers to clients. It is notified of updated sequence numbers as they occur
 * by being inserted into the middle of a replication pipeline.
 * 
 * @author Brett Henderson
 */
public class ReplicationSequenceServer implements ChangeSinkChangeSource {

	private static final Logger LOG = Logger.getLogger(ReplicationSequenceServer.class.getName());

	private ChangeSink changeSink;
	private ReplicationState state;
	private long sequenceNumber;
	private SequenceServer server;
	private boolean serverStarted;


	/**
	 * Creates a new instance.
	 * 
	 * @param port
	 *            The port to listen on.
	 */
	public ReplicationSequenceServer(int port) {
		server = new SequenceServer(port, new SequenceNumberServerChannelPipelineFactory());

		serverStarted = false;
	}


	@Override
	public void setChangeSink(ChangeSink changeSink) {
		this.changeSink = changeSink;
	}


	/**
	 * Returns the port that is being used to listen for new connections.
	 * 
	 * @return The port number.
	 */
	public int getPort() {
		return server.getPort();
	}


	@Override
	public void initialize(Map<String, Object> metaData) {
		// Get the replication state from the upstream task.
		if (!metaData.containsKey(ReplicationState.META_DATA_KEY)) {
			throw new OsmosisRuntimeException("No replication state has been provided in metadata key "
					+ ReplicationState.META_DATA_KEY + ".");
		}
		state = (ReplicationState) metaData.get(ReplicationState.META_DATA_KEY);

		// Call the downstream initialize which will among other things
		// initialise the state.
		changeSink.initialize(metaData);

		// We must only read from the state object during initialize and
		// complete because it may be updated by other threads at other times.
		sequenceNumber = state.getSequenceNumber();

		// If the sequence id is still 0 then replication hasn't been fully
		// initialized and we can't start the server yet.
		if (sequenceNumber > 0 && !serverStarted) {
			// We can start the server now. We give it the previous sequence
			// number because the current one is still in progress.
			server.start(sequenceNumber - 1);
			serverStarted = true;
		}
	}


	@Override
	public void process(ChangeContainer change) {
		changeSink.process(change);
	}


	@Override
	public void complete() {
		changeSink.complete();

		// The sink has completed persisting the replication so now we must
		// notify the server which will notify the listening clients.
		if (!serverStarted) {
			server.start(sequenceNumber);
			serverStarted = true;
		} else {
			server.update(sequenceNumber);
		}
	}


	@Override
	public void release() {
		changeSink.release();

		if (serverStarted) {
			try {
				server.stop();
			} catch (RuntimeException e) {
				LOG.log(Level.WARNING, "Replication sequence server stop failed.", e);
			}

			serverStarted = false;
		}
	}
}
