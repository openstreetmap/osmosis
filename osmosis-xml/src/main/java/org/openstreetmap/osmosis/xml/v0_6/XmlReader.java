// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.xml.v0_6;

import org.openstreetmap.osmosis.core.task.v0_6.RunnableSource;
import org.openstreetmap.osmosis.core.task.v0_6.Sink;

import org.openstreetmap.osmosis.xml.common.CompressionMethod;
import org.openstreetmap.osmosis.xml.v0_6.impl.BaseXMLReader;
import org.openstreetmap.osmosis.xml.v0_6.impl.OsmHandler;

import java.io.File;
import java.util.Collections;


/**
 * An OSM data source reading from an xml file. The entire contents of the file
 * are read.
 * 
 * @author Brett Henderson
 */
public class XmlReader extends BaseXMLReader implements RunnableSource {
	private Sink sink;
	
	/**
	 * Creates a new instance.
	 * 
	 * @param file
	 *            The file to read.
	 * @param enableDateParsing
	 *            If true, dates will be parsed from xml data, else the current
	 *            date will be used thus saving parsing time.
	 * @param compressionMethod
	 *            Specifies the compression method to employ.
	 */
	public XmlReader(File file, boolean enableDateParsing, CompressionMethod compressionMethod) {
        super(file, enableDateParsing, compressionMethod);
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void setSink(Sink sink) {
		this.sink = sink;
	}
	
	/**
	 * Reads all data from the file and send it to the sink.
	 */
	public void run() {
		try {
            this.sink.initialize(Collections.emptyMap());
            this.handleXML(new OsmHandler(this.sink, this.isEnableDateParsing()));
            this.sink.complete();
		} finally {
            this.sink.close();
		}
	}
}
