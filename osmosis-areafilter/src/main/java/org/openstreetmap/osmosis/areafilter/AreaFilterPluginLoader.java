// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.areafilter;

import java.util.HashMap;
import java.util.Map;

import org.openstreetmap.osmosis.areafilter.v0_6.BoundingBoxFilterFactory;
import org.openstreetmap.osmosis.areafilter.v0_6.PolygonFilterFactory;
import org.openstreetmap.osmosis.core.pipeline.common.TaskManagerFactory;
import org.openstreetmap.osmosis.core.plugin.PluginLoader;


/**
 * The plugin loader for the API Schema tasks.
 * 
 * @author Brett Henderson
 */
public class AreaFilterPluginLoader implements PluginLoader {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Map<String, TaskManagerFactory> loadTaskFactories() {
		Map<String, TaskManagerFactory> factoryMap;
		
		factoryMap = new HashMap<String, TaskManagerFactory>();
		
		factoryMap.put("bounding-box", new BoundingBoxFilterFactory());
		factoryMap.put("bb", new BoundingBoxFilterFactory());
		factoryMap.put("bounding-polygon", new PolygonFilterFactory());
		factoryMap.put("bp", new PolygonFilterFactory());
		
		factoryMap.put("bounding-box-0.6", new BoundingBoxFilterFactory());
		factoryMap.put("bounding-polygon-0.6", new PolygonFilterFactory());
		
		return factoryMap;
	}
}
