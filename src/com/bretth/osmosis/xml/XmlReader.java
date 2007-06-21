package com.bretth.osmosis.xml;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.SAXException;

import com.bretth.osmosis.OsmosisRuntimeException;
import com.bretth.osmosis.task.RunnableSource;
import com.bretth.osmosis.task.Sink;
import com.bretth.osmosis.xml.impl.OsmHandler;

/**
 * An OSM data source reading from an xml file. The entire contents of the file
 * are read.
 * 
 * @author Brett Henderson
 */
public class XmlReader implements RunnableSource {
	private Sink sink;
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
	public void setSink(Sink sink) {
		this.sink = sink;
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
			throw new OsmosisRuntimeException("Unable to create SAX Parser.", e);
		} catch (SAXException e) {
			throw new OsmosisRuntimeException("Unable to create SAX Parser.", e);
		}
	}
	
	
	/**
	 * Reads all data from the file and send it to the sink.
	 */
	public void run() {
		try {
			SAXParser parser;
			
			parser = createParser();
			
			parser.parse(file, new OsmHandler(sink));
			
			sink.complete();
			
		} catch (SAXException e) {
			throw new OsmosisRuntimeException("Unable to parse XML.", e);
		} catch (IOException e) {
			throw new OsmosisRuntimeException("Unable to read XML file.", e);
		} finally {
			sink.release();
		}
	}
}
