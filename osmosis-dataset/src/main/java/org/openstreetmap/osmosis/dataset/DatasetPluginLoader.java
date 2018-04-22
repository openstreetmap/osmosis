// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.dataset;

import org.openstreetmap.osmosis.core.pipeline.common.TaskManagerFactory;
import org.openstreetmap.osmosis.core.plugin.PluginLoader;
import org.openstreetmap.osmosis.dataset.v0_6.DatasetBoundingBoxFilterFactory;
import org.openstreetmap.osmosis.dataset.v0_6.DumpDatasetFactory;

import java.util.HashMap;
import java.util.Map;


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
		
		factoryMap.put("dataset-dump", new DumpDatasetFactory());
		factoryMap.put("dd", new DumpDatasetFactory());
		factoryMap.put("dataset-bounding-box", new DatasetBoundingBoxFilterFactory());
		factoryMap.put("dbb", new DatasetBoundingBoxFilterFactory());
		
		factoryMap.put("dataset-dump-0.6", new DumpDatasetFactory());
		factoryMap.put("dataset-bounding-box-0.6", new DatasetBoundingBoxFilterFactory());
		
		return factoryMap;
	}
}
