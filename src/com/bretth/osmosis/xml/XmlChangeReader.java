package com.bretth.osmosis.xml;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.SAXException;

import com.bretth.osmosis.OsmosisRuntimeException;
import com.bretth.osmosis.task.ChangeSink;
import com.bretth.osmosis.task.RunnableChangeSource;
import com.bretth.osmosis.xml.impl.OsmChangeHandler;


/**
 * A change source reading from an xml file. The entire contents of the file
 * are read.
 * 
 * @author Brett Henderson
 */
public class XmlChangeReader implements RunnableChangeSource {
	private ChangeSink changeSink;
	private File file;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param file
	 *            The file to read.
	 */
	public XmlChangeReader(File file) {
		this.file = file;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void setChangeSink(ChangeSink changeSink) {
		this.changeSink = changeSink;
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
			
			parser.parse(file, new OsmChangeHandler(changeSink));
			
			changeSink.complete();
			
		} catch (SAXException e) {
			throw new OsmosisRuntimeException("Unable to parse XML.", e);
		} catch (IOException e) {
			throw new OsmosisRuntimeException("Unable to read XML file.", e);
		} finally {
			changeSink.release();
		}
	}
}
