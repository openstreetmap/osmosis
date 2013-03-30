// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.replicationhttp.v0_6;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.openstreetmap.osmosis.core.container.v0_6.ChangeContainer;
import org.openstreetmap.osmosis.core.container.v0_6.NodeContainer;
import org.openstreetmap.osmosis.core.domain.v0_6.CommonEntityData;
import org.openstreetmap.osmosis.core.domain.v0_6.Node;
import org.openstreetmap.osmosis.core.domain.v0_6.OsmUser;
import org.openstreetmap.osmosis.core.task.common.ChangeAction;
import org.openstreetmap.osmosis.core.task.v0_6.ChangeSink;
import org.openstreetmap.osmosis.core.task.v0_6.ChangeSource;
import org.openstreetmap.osmosis.replication.common.ReplicationState;


/**
 * This test task is used for driving test replication data through a
 * replication pipeline.
 * 
 * @author Brett Henderson
 */
public class MockReplicationSource implements ChangeSource {

	private ChangeSink changeSink;


	@Override
	public void setChangeSink(ChangeSink changeSink) {
		this.changeSink = changeSink;
	}


	/**
	 * Sends a replication sequence containing dummy data to the destination.
	 */
	public void sendSequence() {
		// Initialise the replication stream.
		ReplicationState state = new ReplicationState();
		Map<String, Object> metaData = new HashMap<String, Object>(1);
		metaData.put(ReplicationState.META_DATA_KEY, state);
		changeSink.initialize(metaData);

		// Send the change data unless this is sequence 0 where no data is
		// allowed. We'll only send a single record for simplicity.
		if (state.getSequenceNumber() > 0) {
			// We'll do a create action on the first replication pass, and modify subsequently.
			ChangeAction action;
			if (state.getSequenceNumber() == 1) {
				action = ChangeAction.Create;
			} else {
				action = ChangeAction.Modify;
			}
			
			// Create a change record which data derived from the
			// replication sequence number itself.
			ChangeContainer change = new ChangeContainer(new NodeContainer(new Node(new CommonEntityData(10,
					(int) state.getSequenceNumber(), new Date(state.getSequenceNumber() * 1000), new OsmUser(11,
							"test"), state.getSequenceNumber() * 2), state.getSequenceNumber() * 3,
					state.getSequenceNumber() * 4)), action);
			
			// Send the record downstream.
			changeSink.process(change);
		}
		
		state.setTimestamp(new Date(state.getSequenceNumber() * 1000));
		
		changeSink.complete();
	}


	/**
	 * Releases all downstream resources.
	 */
	public void release() {
		changeSink.release();
	}
}
