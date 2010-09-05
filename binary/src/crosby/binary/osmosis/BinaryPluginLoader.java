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
          
          factoryMap = new HashMap<String, TaskManagerFactory>();
          factoryMap.put("read-bin", new OsmosisReaderFactory());
          factoryMap.put("write-bin", new OsmosisSerializerFactory());

          factoryMap.put("read-bin-0.6", new OsmosisReaderFactory());
          factoryMap.put("write-bin-0.6", new OsmosisSerializerFactory());
          return factoryMap;
    }
  } 
