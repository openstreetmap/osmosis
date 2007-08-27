package com.bretth.osmosis.core.xml;

import java.io.BufferedWriter;
import java.io.File;

import com.bretth.osmosis.core.container.EntityContainer;
import com.bretth.osmosis.core.task.Sink;
import com.bretth.osmosis.core.xml.impl.OsmWriter;


/**
 * An OSM data sink for storing all data to an xml file.
 * 
 * @author Brett Henderson
 */
public class XmlWriter extends BaseXmlWriter implements Sink {
	
	private OsmWriter osmWriter;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param file
	 *            The file to write.
	 * @param compressionMethod
	 *            Specifies the compression method to employ.
	 */
	public XmlWriter(File file, CompressionMethod compressionMethod) {
		super(file, compressionMethod);
		
		osmWriter = new OsmWriter("osm", 0);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void process(EntityContainer entityContainer) {
		initialize();
		
		osmWriter.process(entityContainer);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void beginElementWriter() {
		osmWriter.begin();
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void endElementWriter() {
		osmWriter.end();
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void setWriterOnElementWriter(BufferedWriter writer) {
		osmWriter.setWriter(writer);
	}
}
