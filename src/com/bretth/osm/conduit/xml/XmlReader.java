package com.bretth.osm.conduit.xml;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.SAXException;

import com.bretth.osm.conduit.ConduitRuntimeException;
import com.bretth.osm.conduit.task.OsmRunnableSource;
import com.bretth.osm.conduit.task.OsmSink;
import com.bretth.osm.conduit.xml.impl.OsmHandler;

/**
 * An OSM data source reading from an xml file. The entire contents of the file
 * are read.
 * 
 * @author Brett Henderson
 */
public class XmlReader implements OsmRunnableSource {
	private OsmSink osmSink;
	private File file;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param file
	 *            The file to read.
	 */
	public XmlReader(File file) {
		this.file = file;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void setOsmSink(OsmSink osmSink) {
		this.osmSink = osmSink;
	}
	
	
	/**
	 * Creates a new SAX parser.
	 * 
	 * @return The newly created SAX parser.
	 */
	private SAXParser createParser() {
		try {
			return SAXParserFactory.newInstance().newSAXParser();
			
		} catch (ParserConfigurationException e) {
			throw new ConduitRuntimeException("Unable to create SAX Parser.", e);
		} catch (SAXException e) {
			throw new ConduitRuntimeException("Unable to create SAX Parser.", e);
		}
	}
	
	
	/**
	 * Reads all data from the file and send it to the sink.
	 */
	public void run() {
		try {
			SAXParser parser;
			
			parser = createParser();
			
			parser.parse(file, new OsmHandler(osmSink));
			
			osmSink.complete();
			
		} catch (SAXException e) {
			throw new ConduitRuntimeException("Unable to parse XML.", e);
		} catch (IOException e) {
			throw new ConduitRuntimeException("Unable to read XML file.", e);
		} finally {
			osmSink.release();
		}
	}
}
