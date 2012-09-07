// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.replication.v0_6;

import java.io.File;
import java.util.Map;

import org.openstreetmap.osmosis.core.container.v0_6.ChangeContainer;
import org.openstreetmap.osmosis.core.task.v0_6.ChangeSink;
import org.openstreetmap.osmosis.replication.common.FileReplicationStore;
import org.openstreetmap.osmosis.replication.common.ReplicationState;
import org.openstreetmap.osmosis.replication.common.ReplicationStore;
import org.openstreetmap.osmosis.xml.v0_6.XmlChangeWriter;


/**
 * This class receives replication streams and writes them to replication files.
 * It supports the initialize and complete method being called multiple times to
 * signify multiple replication intervals and each will be written to a
 * different replication file with a unique sequence number.
 * 
 * @author Brett Henderson
 */
public class ReplicationWriter implements ChangeSink {

	private ReplicationStore replicationStore;
	private ReplicationStateWriter stateWriter;
	private ReplicationState state;
	private XmlChangeWriter changeWriter;


	/**
	 * Creates a new instance.
	 * 
	 * @param workingDirectory
	 *            The directory containing configuration and tracking files.
	 */
	public ReplicationWriter(File workingDirectory) {
		replicationStore = new FileReplicationStore(workingDirectory, false);
		stateWriter = new ReplicationStateWriter(workingDirectory);
	}


	@Override
	public void initialize(Map<String, Object> metaData) {
		// Initialise the replication meta data.
		stateWriter.initialize(metaData);

		// Get the replication state for this pipeline run.
		state = (ReplicationState) metaData.get(ReplicationState.META_DATA_KEY);

		// Initialize a new change writer for the current sequence number.
		if (state.getSequenceNumber() > 0) {
			changeWriter = replicationStore.saveData(state.getSequenceNumber());
		}
	}


	@Override
	public void process(ChangeContainer change) {
		changeWriter.process(change);
	}


	@Override
	public void complete() {
		if (state.getSequenceNumber() > 0) {
			// Complete the writing of the change file.
			changeWriter.complete();
			changeWriter.release();
			changeWriter = null;
		}

		// Write the sequenced state file.
		replicationStore.saveState(state);

		// We must only complete the state writer after we've finished writing
		// the replication data and sequence numbered state.
		stateWriter.complete();
	}


	@Override
	public void release() {
		if (changeWriter != null) {
			changeWriter.release();
			changeWriter = null;
		}
		stateWriter.release();
	}
}
