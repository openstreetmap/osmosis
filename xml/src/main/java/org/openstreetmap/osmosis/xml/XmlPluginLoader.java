// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.xml;

import java.util.HashMap;
import java.util.Map;

import org.openstreetmap.osmosis.core.pipeline.common.TaskManagerFactory;
import org.openstreetmap.osmosis.core.plugin.PluginLoader;
import org.openstreetmap.osmosis.xml.v0_6.FastXmlReaderFactory;
import org.openstreetmap.osmosis.xml.v0_6.XmlChangeReaderFactory;
import org.openstreetmap.osmosis.xml.v0_6.XmlChangeUploaderFactory;
import org.openstreetmap.osmosis.xml.v0_6.XmlChangeWriterFactory;
import org.openstreetmap.osmosis.xml.v0_6.XmlDownloaderFactory;
import org.openstreetmap.osmosis.xml.v0_6.XmlReaderFactory;
import org.openstreetmap.osmosis.xml.v0_6.XmlWriterFactory;


/**
 * The plugin loader for the XML tasks.
 * 
 * @author Brett Henderson
 */
public class XmlPluginLoader implements PluginLoader {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Map<String, TaskManagerFactory> loadTaskFactories() {
		Map<String, TaskManagerFactory> factoryMap;
		
		factoryMap = new HashMap<String, TaskManagerFactory>();
		
		factoryMap.put("read-xml", new XmlReaderFactory());
		factoryMap.put("fast-read-xml", new FastXmlReaderFactory());
		factoryMap.put("rx", new XmlReaderFactory());
        factoryMap.put("read-xml-change",  new XmlChangeReaderFactory());
        factoryMap.put("upload-xml-change", new XmlChangeUploaderFactory());
		factoryMap.put("rxc", new XmlChangeReaderFactory());
		factoryMap.put("write-xml", new XmlWriterFactory());
		factoryMap.put("wx", new XmlWriterFactory());
		factoryMap.put("write-xml-change", new XmlChangeWriterFactory());
		factoryMap.put("wxc", new XmlChangeWriterFactory());
		factoryMap.put("read-api", new XmlDownloaderFactory());
		factoryMap.put("ra", new XmlDownloaderFactory());
		
		factoryMap.put("read-xml-0.6", new XmlReaderFactory());
		factoryMap.put("fast-read-xml-0.6", new FastXmlReaderFactory());
		factoryMap.put("read-xml-change-0.6", new XmlChangeReaderFactory());
		factoryMap.put("write-xml-0.6", new XmlWriterFactory());
		factoryMap.put("write-xml-change-0.6", new XmlChangeWriterFactory());
		factoryMap.put("read-api-0.6", new XmlDownloaderFactory());
		
		return factoryMap;
	}
}
