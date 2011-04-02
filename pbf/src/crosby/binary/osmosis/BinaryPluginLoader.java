// This software is released into the Public Domain.  See copying.txt for details.
package crosby.binary.osmosis;

import java.util.HashMap;
import java.util.Map;

import org.openstreetmap.osmosis.core.plugin.PluginLoader;
import org.openstreetmap.osmosis.core.pipeline.common.TaskManagerFactory;

/** Register the binary reading and writing functions. */
public class BinaryPluginLoader implements PluginLoader {
  @Override
  public Map<String, TaskManagerFactory> loadTaskFactories() {
          Map<String, TaskManagerFactory> factoryMap;

          OsmosisReaderFactory reader = new OsmosisReaderFactory();
          OsmosisSerializerFactory writer = new OsmosisSerializerFactory();
          
          factoryMap = new HashMap<String, TaskManagerFactory>();
          factoryMap.put("read-pbf", reader);
          factoryMap.put("read-bin", reader);
          factoryMap.put("rb", reader);
          factoryMap.put("write-pbf", writer);
          factoryMap.put("write-bin", writer);
          factoryMap.put("wb", writer);

          factoryMap.put("read-pbf-0.6", reader);
          factoryMap.put("write-pbf-0.6", writer);
          return factoryMap;
    }
  } 
