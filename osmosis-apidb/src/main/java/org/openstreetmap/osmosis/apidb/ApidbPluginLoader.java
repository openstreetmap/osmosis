// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.apidb;

import java.util.HashMap;
import java.util.Map;

import org.openstreetmap.osmosis.apidb.v0_6.ApidbChangeReaderFactory;
import org.openstreetmap.osmosis.apidb.v0_6.ApidbChangeWriterFactory;
import org.openstreetmap.osmosis.apidb.v0_6.ApidbCurrentReaderFactory;
import org.openstreetmap.osmosis.apidb.v0_6.ApidbFileReplicatorFactory;
import org.openstreetmap.osmosis.apidb.v0_6.ApidbReaderFactory;
import org.openstreetmap.osmosis.apidb.v0_6.ApidbTruncatorFactory;
import org.openstreetmap.osmosis.apidb.v0_6.ApidbWriterFactory;
import org.openstreetmap.osmosis.core.pipeline.common.TaskManagerFactory;
import org.openstreetmap.osmosis.core.plugin.PluginLoader;


/**
 * The plugin loader for the API Schema tasks.
 * 
 * @author Brett Henderson
 */
public class ApidbPluginLoader implements PluginLoader {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Map<String, TaskManagerFactory> loadTaskFactories() {
		Map<String, TaskManagerFactory> factoryMap;
		
		factoryMap = new HashMap<String, TaskManagerFactory>();
		
		factoryMap.put("read-apidb", new ApidbReaderFactory());
		factoryMap.put("rd", new ApidbReaderFactory());
		factoryMap.put("read-apidb-change", new ApidbChangeReaderFactory());
		factoryMap.put("rdc", new ApidbChangeReaderFactory());
		factoryMap.put("read-apidb-current", new ApidbCurrentReaderFactory());
		factoryMap.put("rdcur", new ApidbCurrentReaderFactory());
		factoryMap.put("write-apidb", new ApidbWriterFactory());
		factoryMap.put("wd", new ApidbWriterFactory());
		factoryMap.put("write-apidb-change", new ApidbChangeWriterFactory());
		factoryMap.put("wdc", new ApidbChangeWriterFactory());
		factoryMap.put("truncate-apidb", new ApidbTruncatorFactory());
		factoryMap.put("td", new ApidbTruncatorFactory());
		factoryMap.put("replicate-apidb", new ApidbFileReplicatorFactory());
		factoryMap.put("repa", new ApidbFileReplicatorFactory());
		
		factoryMap.put("read-apidb-0.6", new ApidbReaderFactory());
		factoryMap.put("read-apidb-change-0.6", new ApidbChangeReaderFactory());
		factoryMap.put("read-apidb-current-0.6", new ApidbCurrentReaderFactory());
		factoryMap.put("write-apidb-0.6", new ApidbWriterFactory());
		factoryMap.put("write-apidb-change-0.6", new ApidbChangeWriterFactory());
		factoryMap.put("truncate-apidb-0.6", new ApidbTruncatorFactory());
		factoryMap.put("replicate-apidb-0.6", new ApidbFileReplicatorFactory());
		
		return factoryMap;
	}
}
