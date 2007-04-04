package com.bretth.osm.conduit.xml;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.SAXException;

import com.bretth.osm.conduit.pipeline.OsmSink;
import com.bretth.osm.conduit.pipeline.OsmSource;
import com.bretth.osm.conduit.pipeline.PipelineRuntimeException;
import com.bretth.osm.conduit.xml.impl.OsmHandler;


public class XmlReader implements OsmSource {
	
	private OsmSink osmSink;
	
	
	public XmlReader() {
	}
	
	
	public XmlReader(OsmSink osmSink) {
		this.osmSink = osmSink;
	}
	
	
	public void setOsmSink(OsmSink osmSink) {
		this.osmSink = osmSink;
	}
	
	
	public void process(File file) {
		try {
			SAXParser parser;
			
			parser = createParser();
			
			parser.parse(file, new OsmHandler(osmSink));
			
			osmSink.complete();
			
		} catch (SAXException e) {
			throw new PipelineRuntimeException("Unable to parse XML.", e);
		} catch (IOException e) {
			throw new PipelineRuntimeException("Unable to read XML file.", e);
		} finally {
			osmSink.release();
		}
	}
	
	
	private SAXParser createParser() {
		try {
			return SAXParserFactory.newInstance().newSAXParser();
			
		} catch (ParserConfigurationException e) {
			throw new PipelineRuntimeException("Unable to create SAX Parser.", e);
		} catch (SAXException e) {
			throw new PipelineRuntimeException("Unable to create SAX Parser.", e);
		}
	}
}
