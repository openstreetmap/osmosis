package com.bretth.osmosis.core.xml;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import com.bretth.osmosis.core.container.ChangeContainer;
import com.bretth.osmosis.core.OsmosisRuntimeException;
import com.bretth.osmosis.core.task.ChangeSink;
import com.bretth.osmosis.core.xml.impl.OsmChangeWriter;


/**
 * An OSM change sink for storing all data to an xml file.
 * 
 * @author Brett Henderson
 */
public class XmlChangeWriter implements ChangeSink {
	
	private OsmChangeWriter osmChangeWriter;
	private File file;
	private boolean initialized;
	private BufferedWriter writer;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param file
	 *            The file to write.
	 */
	public XmlChangeWriter(File file) {
		this.file = file;
		
		osmChangeWriter = new OsmChangeWriter("osmChange", 0);
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
			
			osmChangeWriter.setWriter(writer);
			
			initialized = true;
			
			write("<?xml version='1.0' encoding='UTF-8'?>");
			writeNewLine();
			
			osmChangeWriter.begin();
		}
	}
	

	/**
	 * {@inheritDoc}
	 */
	public void process(ChangeContainer changeContainer) {
		initialize();
		osmChangeWriter.process(changeContainer);
	}
	
	
	/**
	 * Flushes all changes to file.
	 */
	public void complete() {
		try {
			if (writer != null) {
				osmChangeWriter.end();
				
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
