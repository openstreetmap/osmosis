// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.xml.v0_6;

import java.io.BufferedWriter;
import java.io.File;
import java.util.Map;

import org.openstreetmap.osmosis.core.container.v0_6.EntityContainer;
import org.openstreetmap.osmosis.core.task.v0_6.Sink;
import org.openstreetmap.osmosis.xml.common.BaseXmlWriter;
import org.openstreetmap.osmosis.xml.common.CompressionMethod;
import org.openstreetmap.osmosis.xml.v0_6.impl.OsmWriter;


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
	 * @param writer
	 *            The writer to send all data to.
	 */
	public XmlWriter(BufferedWriter writer) {
		super(writer);
		
		osmWriter = new OsmWriter("osm", 0, true, false);
	}
	
	
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
		
		osmWriter = new OsmWriter("osm", 0, true, false);
	}
    
    
    /**
     * {@inheritDoc}
     */
    public void initialize(Map<String, Object> metaData) {
		// Do nothing.
	}
	

	/**
	 * Creates a new instance.
	 * 
	 * @param file
	 *            The file to write.
	 * @param compressionMethod
	 *            Specifies the compression method to employ.
	 * @param legacyBound
	 *            If true, write the legacy <bound> element instead of the
	 *            correct <bounds> one.
	 */
	public XmlWriter(File file, CompressionMethod compressionMethod, boolean legacyBound) {
		super(file, compressionMethod);
		
		osmWriter = new OsmWriter("osm", 0, true, legacyBound);
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
