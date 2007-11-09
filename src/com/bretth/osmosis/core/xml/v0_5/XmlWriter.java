package com.bretth.osmosis.core.xml.v0_5;

import java.io.BufferedWriter;
import java.io.File;

import com.bretth.osmosis.core.container.v0_5.EntityContainer;
import com.bretth.osmosis.core.task.v0_5.Sink;
import com.bretth.osmosis.core.xml.common.BaseXmlWriter;
import com.bretth.osmosis.core.xml.common.CompressionMethod;
import com.bretth.osmosis.core.xml.v0_5.impl.OsmWriter;


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
	 * @param enableProdEncodingHack
	 *            If true, a special encoding is enabled which works around an
	 *            encoding issue with the current production configuration where
	 *            data is double encoded as utf-8.
	 */
	public XmlWriter(File file, CompressionMethod compressionMethod, boolean enableProdEncodingHack) {
		super(file, compressionMethod, enableProdEncodingHack);
		
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
