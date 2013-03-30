// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.xml.v0_6;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.openstreetmap.osmosis.core.OsmosisRuntimeException;
import org.openstreetmap.osmosis.core.task.v0_6.ChangeSink;
import org.openstreetmap.osmosis.core.task.v0_6.RunnableChangeSource;
import org.openstreetmap.osmosis.xml.common.CompressionActivator;
import org.openstreetmap.osmosis.xml.common.CompressionMethod;
import org.openstreetmap.osmosis.xml.v0_6.impl.OsmChangeHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;


/**
 * A change source reading from an xml file. The entire contents of the file
 * are read.
 * 
 * @author Brett Henderson
 */
public class XmlChangeReader implements RunnableChangeSource {
	
	private static Logger log = Logger.getLogger(XmlReader.class.getName());
	
	private ChangeSink changeSink;
	private File file;
	private boolean enableDateParsing;
	private CompressionMethod compressionMethod;
	
	
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
	public XmlChangeReader(File file, boolean enableDateParsing, CompressionMethod compressionMethod) {
		this.file = file;
		this.enableDateParsing = enableDateParsing;
		this.compressionMethod = compressionMethod;
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
		InputStream inputStream = null;
		
		try {
			SAXParser parser;
			
			changeSink.initialize(Collections.<String, Object>emptyMap());
			
			// make "-" an alias for /dev/stdin
			if (file.getName().equals("-")) {
				inputStream = System.in;
			} else {
				inputStream = new FileInputStream(file);
			}
			
			inputStream =
				new CompressionActivator(compressionMethod).
					createCompressionInputStream(inputStream);
			
			parser = createParser();
			
			parser.parse(inputStream, new OsmChangeHandler(changeSink, enableDateParsing));
			
			changeSink.complete();
			
		} catch (SAXParseException e) {
			throw new OsmosisRuntimeException(
				"Unable to parse xml file " + file
				+ ".  publicId=(" + e.getPublicId()
				+ "), systemId=(" + e.getSystemId()
				+ "), lineNumber=" + e.getLineNumber()
				+ ", columnNumber=" + e.getColumnNumber() + ".",
				e);
		} catch (SAXException e) {
			throw new OsmosisRuntimeException("Unable to parse XML.", e);
		} catch (IOException e) {
			throw new OsmosisRuntimeException("Unable to read XML file " + file + ".", e);
		} finally {
			changeSink.release();
			
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException e) {
					log.log(Level.SEVERE, "Unable to close input stream.", e);
				}
				inputStream = null;
			}
		}
	}
}
