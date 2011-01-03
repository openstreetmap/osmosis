/**
 * Copyright 2010 Scott A. Crosby
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * @author Scott A. Crosby <scott@sacrosby.com>
 *
 */
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
