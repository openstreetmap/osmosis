// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.dataset;

import java.util.HashMap;
import java.util.Map;

import org.openstreetmap.osmosis.core.pipeline.common.TaskManagerFactory;
import org.openstreetmap.osmosis.core.plugin.PluginLoader;
import org.openstreetmap.osmosis.dataset.v0_6.DatasetBoundingBoxFilterFactory;
import org.openstreetmap.osmosis.dataset.v0_6.DumpDatasetFactory;
import org.openstreetmap.osmosis.dataset.v0_6.ReadDatasetFactory;
import org.openstreetmap.osmosis.dataset.v0_6.WriteDatasetFactory;


/**
 * The plugin loader for the dataset tasks.
 * 
 * @author Brett Henderson
 */
public class DatasetPluginLoader implements PluginLoader {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Map<String, TaskManagerFactory> loadTaskFactories() {
		Map<String, TaskManagerFactory> factoryMap;
		
		factoryMap = new HashMap<String, TaskManagerFactory>();
		
		factoryMap.put("write-customdb", new WriteDatasetFactory());
		factoryMap.put("wc", new WriteDatasetFactory());
		factoryMap.put("dataset-dump", new DumpDatasetFactory());
		factoryMap.put("dd", new DumpDatasetFactory());
		factoryMap.put("read-customdb", new ReadDatasetFactory());
		factoryMap.put("rc", new ReadDatasetFactory());
		factoryMap.put("dataset-bounding-box", new DatasetBoundingBoxFilterFactory());
		factoryMap.put("dbb", new DatasetBoundingBoxFilterFactory());
		
		factoryMap.put("write-customdb-0.6", new WriteDatasetFactory());
		factoryMap.put("dataset-dump-0.6", new DumpDatasetFactory());
		factoryMap.put("read-customdb-0.6", new ReadDatasetFactory());
		factoryMap.put("dataset-bounding-box-0.6", new DatasetBoundingBoxFilterFactory());
		
		return factoryMap;
	}
}
