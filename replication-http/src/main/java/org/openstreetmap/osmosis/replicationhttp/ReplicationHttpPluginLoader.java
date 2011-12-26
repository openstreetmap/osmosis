// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.replicationhttp;

import java.util.HashMap;
import java.util.Map;

import org.openstreetmap.osmosis.core.pipeline.common.TaskManagerFactory;
import org.openstreetmap.osmosis.core.plugin.PluginLoader;
import org.openstreetmap.osmosis.replicationhttp.v0_6.ReplicationDataServerFactory;
import org.openstreetmap.osmosis.replicationhttp.v0_6.ReplicationSequenceServerFactory;


/**
 * The plugin loader for the API Schema tasks.
 * 
 * @author Brett Henderson
 */
public class ReplicationHttpPluginLoader implements PluginLoader {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Map<String, TaskManagerFactory> loadTaskFactories() {
		Map<String, TaskManagerFactory> factoryMap;
		
		factoryMap = new HashMap<String, TaskManagerFactory>();

		factoryMap.put("serve-replication-data", new ReplicationDataServerFactory());
		factoryMap.put("srd", new ReplicationDataServerFactory());
		factoryMap.put("serve-replication-sequence", new ReplicationSequenceServerFactory());
		factoryMap.put("srs", new ReplicationSequenceServerFactory());
		
		factoryMap.put("serve-replication-data-0.6", new ReplicationDataServerFactory());
		factoryMap.put("serve-replication-sequence-0.6", new ReplicationSequenceServerFactory());
		
		return factoryMap;
	}
}
