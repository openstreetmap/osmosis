// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package com.bretth.osmosis.core.xml.v0_6;

import java.io.BufferedWriter;
import java.io.File;

import com.bretth.osmosis.core.container.v0_6.ChangeContainer;
import com.bretth.osmosis.core.task.v0_6.ChangeSink;
import com.bretth.osmosis.core.xml.common.BaseXmlWriter;
import com.bretth.osmosis.core.xml.common.CompressionMethod;
import com.bretth.osmosis.core.xml.v0_6.impl.OsmChangeWriter;


/**
 * An OSM change sink for storing all data to an xml file.
 * 
 * @author Brett Henderson
 */
public class XmlChangeWriter extends BaseXmlWriter implements ChangeSink {
	
	private OsmChangeWriter osmChangeWriter;
	
	
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
	public XmlChangeWriter(File file, CompressionMethod compressionMethod, boolean enableProdEncodingHack) {
		super(file, compressionMethod, enableProdEncodingHack);
		
		osmChangeWriter = new OsmChangeWriter("osmChange", 0);
	}
	

	/**
	 * {@inheritDoc}
	 */
	public void process(ChangeContainer changeContainer) {
		initialize();
		
		osmChangeWriter.process(changeContainer);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void beginElementWriter() {
		osmChangeWriter.begin();
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void endElementWriter() {
		osmChangeWriter.end();
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void setWriterOnElementWriter(BufferedWriter writer) {
		osmChangeWriter.setWriter(writer);
	}
}
