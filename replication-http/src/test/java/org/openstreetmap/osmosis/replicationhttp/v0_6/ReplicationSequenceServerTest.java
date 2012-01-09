// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.replicationhttp.v0_6;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.openstreetmap.osmosis.replication.common.ReplicationState;
import org.openstreetmap.osmosis.testutil.AbstractDataTest;


/**
 * Tests the replication sequence server task.
 * 
 * @author Brett Henderson
 */
public class ReplicationSequenceServerTest extends AbstractDataTest {

	/**
	 * Very basic test that launches the server, runs several replication
	 * iterations and then shuts down without connecting any clients.
	 * 
	 * @throws InterruptedException
	 *             if processing is interrupted.
	 */
	@Test
	public void testStartupShutdown() throws InterruptedException {
		ReplicationSequenceServer server;

		server = new ReplicationSequenceServer(0);
		server.setChangeSink(new MockReplicationDestination());

		try {
			for (int i = 0; i < 10; i++) {
				ReplicationState state = new ReplicationState();
				Map<String, Object> metaData = new HashMap<String, Object>();
				metaData.put(ReplicationState.META_DATA_KEY, state);
				server.initialize(metaData);
				Thread.sleep(10);
				server.complete();
			}
		} finally {
			server.release();
		}
	}
}
