// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.pgsnapshot;

import java.util.HashMap;
import java.util.Map;

import org.openstreetmap.osmosis.core.pipeline.common.TaskManagerFactory;
import org.openstreetmap.osmosis.core.plugin.PluginLoader;
import org.openstreetmap.osmosis.pgsnapshot.v0_6.PostgreSqlChangeWriterFactory;
import org.openstreetmap.osmosis.pgsnapshot.v0_6.PostgreSqlCopyWriterFactory;
import org.openstreetmap.osmosis.pgsnapshot.v0_6.PostgreSqlDatasetReaderFactory;
import org.openstreetmap.osmosis.pgsnapshot.v0_6.PostgreSqlDumpWriterFactory;
import org.openstreetmap.osmosis.pgsnapshot.v0_6.PostgreSqlTruncatorFactory;


/**
 * The plugin loader for the PostgreSQL Snapshot Schema tasks.
 * 
 * @author Brett Henderson
 */
public class PgSnapshotPluginLoader implements PluginLoader {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Map<String, TaskManagerFactory> loadTaskFactories() {
		Map<String, TaskManagerFactory> factoryMap;
		
		factoryMap = new HashMap<String, TaskManagerFactory>();
		
		factoryMap.put("write-pgsql", new PostgreSqlCopyWriterFactory());
		factoryMap.put("wp", new PostgreSqlCopyWriterFactory());
		factoryMap.put("truncate-pgsql", new PostgreSqlTruncatorFactory());
		factoryMap.put("tp", new PostgreSqlTruncatorFactory());
		factoryMap.put("write-pgsql-dump", new PostgreSqlDumpWriterFactory());
		factoryMap.put("wpd", new PostgreSqlDumpWriterFactory());
		factoryMap.put("read-pgsql", new PostgreSqlDatasetReaderFactory());
		factoryMap.put("rp", new PostgreSqlDatasetReaderFactory());
		factoryMap.put("write-pgsql-change", new PostgreSqlChangeWriterFactory());
		factoryMap.put("wpc", new PostgreSqlChangeWriterFactory());
		
		factoryMap.put("write-pgsql-0.6", new PostgreSqlCopyWriterFactory());
		factoryMap.put("truncate-pgsql-0.6", new PostgreSqlTruncatorFactory());
		factoryMap.put("write-pgsql-dump-0.6", new PostgreSqlDumpWriterFactory());
		factoryMap.put("read-pgsql-0.6", new PostgreSqlDatasetReaderFactory());
		factoryMap.put("write-pgsql-change-0.6", new PostgreSqlChangeWriterFactory());
		
		return factoryMap;
	}
}
