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


public class XmlReader implements OsmRunnableSource {
	
	private OsmSink osmSink;
	private File file;
	
	
	public XmlReader() {
		// Nothing to do here.
	}
	
	
	public XmlReader(OsmSink osmSink) {
		this.osmSink = osmSink;
	}
	
	
	public void setOsmSink(OsmSink osmSink) {
		this.osmSink = osmSink;
	}
	
	
	public void setFile(File file) {
		this.file = file;
	}
	
	
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
	
	
	private SAXParser createParser() {
		try {
			return SAXParserFactory.newInstance().newSAXParser();
			
		} catch (ParserConfigurationException e) {
			throw new ConduitRuntimeException("Unable to create SAX Parser.", e);
		} catch (SAXException e) {
			throw new ConduitRuntimeException("Unable to create SAX Parser.", e);
		}
	}
}
