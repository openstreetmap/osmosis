// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.replication;

import java.util.HashMap;
import java.util.Map;

import org.openstreetmap.osmosis.core.pipeline.common.TaskManagerFactory;
import org.openstreetmap.osmosis.core.plugin.PluginLoader;
import org.openstreetmap.osmosis.replication.v0_6.IntervalDownloaderFactory;
import org.openstreetmap.osmosis.replication.v0_6.IntervalDownloaderInitializerFactory;
import org.openstreetmap.osmosis.replication.v0_6.ReplicationDownloaderFactory;
import org.openstreetmap.osmosis.replication.v0_6.ReplicationDownloaderInitializerFactory;
import org.openstreetmap.osmosis.replication.v0_6.ReplicationFileMergerFactory;
import org.openstreetmap.osmosis.replication.v0_6.ReplicationFileMergerInitializerFactory;
import org.openstreetmap.osmosis.replication.v0_6.ReplicationLagReaderFactory;
import org.openstreetmap.osmosis.replication.v0_6.ReplicationToChangeWriterFactory;
import org.openstreetmap.osmosis.replication.v0_6.ReplicationWriterFactory;


/**
 * The plugin loader for the replication tasks.
 * 
 * @author Brett Henderson
 */
public class ReplicationPluginLoader implements PluginLoader {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Map<String, TaskManagerFactory> loadTaskFactories() {
		Map<String, TaskManagerFactory> factoryMap;
		
		factoryMap = new HashMap<String, TaskManagerFactory>();
		
		factoryMap.put("read-change-interval", new IntervalDownloaderFactory());
		factoryMap.put("rci", new IntervalDownloaderFactory());
		factoryMap.put("read-change-interval-init", new IntervalDownloaderInitializerFactory());
		factoryMap.put("rcii", new IntervalDownloaderInitializerFactory());
		factoryMap.put("read-replication-interval", new ReplicationDownloaderFactory());
		factoryMap.put("rri", new ReplicationDownloaderFactory());
		factoryMap.put("read-replication-interval-init", new ReplicationDownloaderInitializerFactory());
		factoryMap.put("rrii", new ReplicationDownloaderInitializerFactory());
		factoryMap.put("merge-replication-files", new ReplicationFileMergerFactory());
		factoryMap.put("mrf", new ReplicationFileMergerFactory());
		factoryMap.put("merge-replication-files-init", new ReplicationFileMergerInitializerFactory());
		factoryMap.put("mrfi", new ReplicationFileMergerInitializerFactory());
		factoryMap.put("read-replication-lag", new ReplicationLagReaderFactory());
		factoryMap.put("rrl", new ReplicationLagReaderFactory());
		factoryMap.put("write-replication", new ReplicationWriterFactory());
		factoryMap.put("wr", new ReplicationWriterFactory());
		factoryMap.put("replication-to-change", new ReplicationToChangeWriterFactory());
		factoryMap.put("rtc", new ReplicationToChangeWriterFactory());
		
		factoryMap.put("read-change-interval-0.6", new IntervalDownloaderFactory());
		factoryMap.put("read-change-interval-init-0.6", new IntervalDownloaderInitializerFactory());
		factoryMap.put("read-replication-interval-0.6", new ReplicationDownloaderFactory());
		factoryMap.put("read-replication-interval-init-0.6", new ReplicationDownloaderInitializerFactory());
		factoryMap.put("merge-replication-files-0.6", new ReplicationFileMergerFactory());
		factoryMap.put("merge-replication-files-init-0.6", new ReplicationFileMergerInitializerFactory());
		factoryMap.put("read-replication-lag-0.6", new ReplicationLagReaderFactory());
		factoryMap.put("write-replication-0.6", new ReplicationWriterFactory());
		factoryMap.put("replication-to-change-0.6", new ReplicationToChangeWriterFactory());
		
		return factoryMap;
	}
}
