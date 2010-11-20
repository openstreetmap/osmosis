// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.pgsimple;

import java.util.HashMap;
import java.util.Map;

import org.openstreetmap.osmosis.core.pipeline.common.TaskManagerFactory;
import org.openstreetmap.osmosis.core.plugin.PluginLoader;
import org.openstreetmap.osmosis.pgsimple.v0_6.PostgreSqlChangeWriterFactory;
import org.openstreetmap.osmosis.pgsimple.v0_6.PostgreSqlCopyWriterFactory;
import org.openstreetmap.osmosis.pgsimple.v0_6.PostgreSqlDatasetReaderFactory;
import org.openstreetmap.osmosis.pgsimple.v0_6.PostgreSqlDumpWriterFactory;
import org.openstreetmap.osmosis.pgsimple.v0_6.PostgreSqlTruncatorFactory;
import org.openstreetmap.osmosis.pgsimple.v0_6.PostgreSqlWriterFactory;


/**
 * The plugin loader for the PostgreSQL Snapshot Schema tasks.
 * 
 * @author Brett Henderson
 */
public class PgSimplePluginLoader implements PluginLoader {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Map<String, TaskManagerFactory> loadTaskFactories() {
		Map<String, TaskManagerFactory> factoryMap;
		
		factoryMap = new HashMap<String, TaskManagerFactory>();
		
		factoryMap.put("write-pgsimp", new PostgreSqlWriterFactory());
		factoryMap.put("ws", new PostgreSqlWriterFactory());
		factoryMap.put("fast-write-pgsimp", new PostgreSqlCopyWriterFactory());
		factoryMap.put("fws", new PostgreSqlCopyWriterFactory());
		factoryMap.put("truncate-pgsimp", new PostgreSqlTruncatorFactory());
		factoryMap.put("ts", new PostgreSqlTruncatorFactory());
		factoryMap.put("write-pgsimp-dump", new PostgreSqlDumpWriterFactory());
		factoryMap.put("wsd", new PostgreSqlDumpWriterFactory());
		factoryMap.put("read-pgsimp", new PostgreSqlDatasetReaderFactory());
		factoryMap.put("rs", new PostgreSqlDatasetReaderFactory());
		factoryMap.put("write-pgsimp-change", new PostgreSqlChangeWriterFactory());
		factoryMap.put("wsc", new PostgreSqlChangeWriterFactory());
		
		factoryMap.put("write-pgsimp-0.6", new PostgreSqlWriterFactory());
		factoryMap.put("fast-write-pgsimp-0.6", new PostgreSqlCopyWriterFactory());
		factoryMap.put("truncate-pgsimp-0.6", new PostgreSqlTruncatorFactory());
		factoryMap.put("write-pgsimp-dump-0.6", new PostgreSqlDumpWriterFactory());
		factoryMap.put("read-pgsimp-0.6", new PostgreSqlDatasetReaderFactory());
		factoryMap.put("write-pgsimp-change-0.6", new PostgreSqlChangeWriterFactory());
		
		return factoryMap;
	}
}
