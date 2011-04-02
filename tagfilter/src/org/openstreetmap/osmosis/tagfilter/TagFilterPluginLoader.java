// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.tagfilter;

import java.util.HashMap;
import java.util.Map;

import org.openstreetmap.osmosis.tagfilter.v0_6.NodeKeyFilterFactory;
import org.openstreetmap.osmosis.tagfilter.v0_6.NodeKeyValueFilterFactory;
import org.openstreetmap.osmosis.tagfilter.v0_6.TagFilterFactory;
import org.openstreetmap.osmosis.tagfilter.v0_6.TagRemoverFactory;
import org.openstreetmap.osmosis.tagfilter.v0_6.UsedNodeFilterFactory;
import org.openstreetmap.osmosis.tagfilter.v0_6.UsedWayFilterFactory;
import org.openstreetmap.osmosis.tagfilter.v0_6.WayKeyFilterFactory;
import org.openstreetmap.osmosis.tagfilter.v0_6.WayKeyValueFilterFactory;
import org.openstreetmap.osmosis.core.pipeline.common.TaskManagerFactory;
import org.openstreetmap.osmosis.core.plugin.PluginLoader;


/**
 * The plugin loader for the Set manipulation tasks.
 * 
 * @author Brett Henderson
 */
public class TagFilterPluginLoader implements PluginLoader {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Map<String, TaskManagerFactory> loadTaskFactories() {
		Map<String, TaskManagerFactory> factoryMap;
		
		factoryMap = new HashMap<String, TaskManagerFactory>();
		
		factoryMap.put("used-node", new UsedNodeFilterFactory());
		factoryMap.put("un", new UsedNodeFilterFactory());
		factoryMap.put("used-way", new UsedWayFilterFactory());
		factoryMap.put("uw", new UsedWayFilterFactory());
		factoryMap.put("tag-filter", new TagFilterFactory());
		factoryMap.put("tf", new TagFilterFactory());
		factoryMap.put("node-key", new NodeKeyFilterFactory());
		factoryMap.put("nk", new NodeKeyFilterFactory());
		factoryMap.put("node-key-value", new NodeKeyValueFilterFactory());
		factoryMap.put("nkv", new NodeKeyValueFilterFactory());
		factoryMap.put("way-key", new WayKeyFilterFactory());
		factoryMap.put("wk", new WayKeyFilterFactory());
		factoryMap.put("way-key-value", new WayKeyValueFilterFactory());
		factoryMap.put("wkv", new WayKeyValueFilterFactory());
		
		factoryMap.put("used-node-0.6", new UsedNodeFilterFactory());
		factoryMap.put("used-way-0.6", new UsedWayFilterFactory());
		factoryMap.put("tag-filter-0.6", new TagFilterFactory());
		factoryMap.put("node-key-0.6", new NodeKeyFilterFactory());
		factoryMap.put("node-key-value-0.6", new NodeKeyValueFilterFactory());
		factoryMap.put("way-key-0.6", new WayKeyFilterFactory());
		factoryMap.put("way-key-value-0.6", new WayKeyValueFilterFactory());
		factoryMap.put("remove-tags-0.6", new TagRemoverFactory());
		
		return factoryMap;
	}
}
