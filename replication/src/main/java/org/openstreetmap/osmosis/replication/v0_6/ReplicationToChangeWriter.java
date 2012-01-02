// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.replication.v0_6;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.openstreetmap.osmosis.core.container.v0_6.ChangeContainer;
import org.openstreetmap.osmosis.core.task.v0_6.ChangeSink;
import org.openstreetmap.osmosis.core.task.v0_6.ChangeSinkChangeSource;
import org.openstreetmap.osmosis.replication.common.ReplicationState;


/**
 * This task allows a replication stream to be converted to a standard change
 * stream. It handles the state persistence required by a replication sink, and
 * then passes the replication data to a standard change sink destination. A
 * typical use case would be receiving a replication stream live from a
 * database, then applying those changes to another database where the change
 * applier task doesn't support the replication metadata extensions.
 * 
 * @author Brett Henderson
 */
public class ReplicationToChangeWriter implements ChangeSinkChangeSource {

	/**
	 * This handles and persists the replication metadata.
	 */
	private ReplicationStateWriter stateWriter;
	private ReplicationState state;
	private ChangeSink changeSink;


	/**
	 * Creates a new instance.
	 * 
	 * @param workingDirectory
	 *            The directory containing configuration and tracking files.
	 */
	public ReplicationToChangeWriter(File workingDirectory) {
		stateWriter = new ReplicationStateWriter(workingDirectory);
	}


	@Override
	public void setChangeSink(ChangeSink changeSink) {
		this.changeSink = changeSink;
	}


	@Override
	public void initialize(Map<String, Object> metaData) {
		// Initialise the replication meta data.
		stateWriter.initialize(metaData);

		// Get the replication state for this pipeline run.
		state = (ReplicationState) metaData.get(ReplicationState.META_DATA_KEY);

		// Initialise the downstream tasks passing everything except the
		// replication state.
		if (state.getSequenceNumber() > 0) {
			Map<String, Object> downstreamMetaData = new HashMap<String, Object>(metaData);
			downstreamMetaData.remove(ReplicationState.META_DATA_KEY);
			changeSink.initialize(downstreamMetaData);
		}
	}


	@Override
	public void process(ChangeContainer change) {
		// Perform replication checks.
		stateWriter.process(change);

		// Pass the change downstream.
		changeSink.process(change);
	}


	@Override
	public void complete() {
		// We must complete downstream before we complete the replication writer
		// so that we know the replication data has been committed before we
		// persist replication state.
		if (state.getSequenceNumber() > 0) {
			changeSink.complete();
		}
		stateWriter.complete();
	}


	@Override
	public void release() {
		changeSink.release();
		stateWriter.release();
	}
}
