// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.xml.v0_6;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.openstreetmap.osmosis.core.task.v0_6.RunnableSource;
import org.openstreetmap.osmosis.core.task.v0_6.Sink;

import org.openstreetmap.osmosis.xml.common.CompressionMethod;
import org.openstreetmap.osmosis.xml.v0_6.impl.BaseXMLReader;
import org.openstreetmap.osmosis.xml.v0_6.impl.FastXmlParser;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * An OSM data source reading from an xml file. The entire contents of the file
 * are read.
 * 
 * @author Jiri Clement
 * @author Brett Henderson
 */
public class FastXmlReader extends BaseXMLReader implements RunnableSource {
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
	public FastXmlReader(File file, boolean enableDateParsing, CompressionMethod compressionMethod) {
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
            this.handleXML(null);
            this.sink.complete();
        } finally {
            this.sink.close();
        }
	}

    @Override
    protected void parseXML(InputStream stream, DefaultHandler handler)
                    throws SAXException, IOException {
        try {
            final XMLInputFactory factory = XMLInputFactory.newInstance();
            factory.setProperty(XMLInputFactory.IS_COALESCING, false);
            factory.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, false);
            factory.setProperty(XMLInputFactory.IS_VALIDATING, false);
            final XMLStreamReader xpp = factory.createXMLStreamReader(stream);

            final FastXmlParser parser = new FastXmlParser(this.sink, xpp, this.isEnableDateParsing());
            parser.readOsm();
        } catch (final XMLStreamException e) {
            throw new SAXException(e);
        }
    }
}
