package com.bretth.osm.osmosis.xml;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import com.bretth.osm.osmosis.OsmosisRuntimeException;
import com.bretth.osm.osmosis.data.Node;
import com.bretth.osm.osmosis.data.Segment;
import com.bretth.osm.osmosis.data.Way;
import com.bretth.osm.osmosis.task.Sink;
import com.bretth.osm.osmosis.xml.impl.OsmWriter;


/**
 * An OSM data sink for storing all data to an xml file.
 * 
 * @author Brett Henderson
 */
public class XmlWriter implements Sink {
	
	private OsmWriter osmWriter;
	private File file;
	private boolean initialized;
	private BufferedWriter writer;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param file
	 *            The file to write.
	 */
	public XmlWriter(File file) {
		this.file = file;
		
		osmWriter = new OsmWriter("osm", 0);
	}
	
	
	/**
	 * Writes data to the output file.
	 * 
	 * @param data
	 *            The data to be written.
	 */
	protected void write(String data) {
		try {
			writer.write(data);
			
		} catch (IOException e) {
			throw new OsmosisRuntimeException("Unable to write data.", e);
		}
	}
	
	
	/**
	 * Writes a new line in the output file.
	 */
	private void writeNewLine() {
		try {
			writer.newLine();
			
		} catch (IOException e) {
			throw new OsmosisRuntimeException("Unable to write data.", e);
		}
	}
	
	
	/**
	 * Initialises the output file for writing. This must be called by
	 * sub-classes before any writing is performed. This method may be called
	 * multiple times without adverse affect allowing sub-classes to invoke it
	 * every time they perform processing.
	 */
	private void initialize() {
		if (!initialized) {
			try {
				writer = new BufferedWriter(new FileWriter(file));
				
			} catch (IOException e) {
				throw new OsmosisRuntimeException("Unable to open file for writing.", e);
			}
			
			initialized = true;
			
			write("<?xml version='1.0' encoding='UTF-8'?>");
			writeNewLine();
			
			osmWriter.begin(writer);
		}
	}
	

	/**
	 * {@inheritDoc}
	 */
	public void processNode(Node node) {
		initialize();
		osmWriter.processNode(writer, node);
	}


	/**
	 * {@inheritDoc}
	 */
	public void processSegment(Segment segment) {
		initialize();
		osmWriter.processSegment(writer, segment);
	}


	/**
	 * {@inheritDoc}
	 */
	public void processWay(Way way) {
		initialize();
		osmWriter.processWay(writer, way);
	}
	
	
	/**
	 * Flushes all changes to file.
	 */
	public void complete() {
		try {
			if (writer != null) {
				osmWriter.end(writer);
				
				writer.flush();
			}
			
		} catch (Exception e) {
			// Do nothing
		} finally {
			writer = null;
			initialized = false;
		}
	}
	
	
	/**
	 * Cleans up any open file handles.
	 */
	public void release() {
		try {
			if (writer != null) {
				writer.close();
			}
			
		} catch (Exception e) {
			// Do nothing
		} finally {
			writer = null;
			initialized = false;
		}
	}
}
