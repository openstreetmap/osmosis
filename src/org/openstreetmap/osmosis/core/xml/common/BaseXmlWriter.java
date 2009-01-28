// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package org.openstreetmap.osmosis.core.xml.common;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.openstreetmap.osmosis.core.OsmosisRuntimeException;


/**
 * An OSM data sink for storing all data to an xml file.
 * 
 * @author Brett Henderson
 */
public abstract class BaseXmlWriter {
	
	private static Logger log = Logger.getLogger(BaseXmlWriter.class.getName());
	
	
	private File file;
	private boolean initialized;
	private BufferedWriter writer;
	private CompressionMethod compressionMethod;
	private boolean enableProdEncodingHack;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param file
	 *            The file to write.
	 * @param compressionMethod
	 *            Specifies the compression method to employ.
	 * @param enableProdEncodingHack
	 *            If true, a special encoding is enabled which works around an
	 *            encoding issue with the current production configuration where
	 *            data is double encoded as utf-8.
	 */
	public BaseXmlWriter(File file, CompressionMethod compressionMethod, boolean enableProdEncodingHack) {
		this.file = file;
		this.compressionMethod = compressionMethod;
		this.enableProdEncodingHack = enableProdEncodingHack;
	}
	
	
	/**
	 * Sets the writer on the element writer used for this implementation.
	 * 
	 * @param writer
	 *            The writer receiving xml data.
	 */
	protected abstract void setWriterOnElementWriter(BufferedWriter writer);
	
	
	/**
	 * Calls the begin method of the element writer used for this implementation.
	 */
	protected abstract void beginElementWriter();
	
	
	/**
	 * Calls the end method of the element writer used for this implementation.
	 */
	protected abstract void endElementWriter();
	
	
	/**
	 * Writes data to the output file.
	 * 
	 * @param data
	 *            The data to be written.
	 */
	private void write(String data) {
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
	protected void initialize() {
		if (!initialized) {
			OutputStream outStream = null;
			
			try {
				OutputStreamWriter outStreamWriter;
				
				// make "-" an alias for /dev/stdout
				if(file.getName().equals("-")) {
					outStream = System.out;
				} else {
					outStream = new FileOutputStream(file);
				}
				
				outStream =
					new CompressionActivator(compressionMethod).createCompressionOutputStream(outStream);
				
				if (enableProdEncodingHack) {
					outStreamWriter = new OutputStreamWriter(outStream, new ProductionDbCharset());
				} else {
					outStreamWriter = new OutputStreamWriter(outStream, "UTF-8");
				}
				
				writer = new BufferedWriter(outStreamWriter);
				
				outStream = null;
				
			} catch (IOException e) {
				throw new OsmosisRuntimeException("Unable to open file for writing.", e);
			} finally {
				if (outStream != null) {
					try {
						outStream.close();
					} catch (Exception e) {
						log.log(Level.SEVERE, "Unable to close output stream.", e);
					}
					outStream = null;
				}
			}
			
			setWriterOnElementWriter(writer);
			
			initialized = true;
			
			write("<?xml version='1.0' encoding='UTF-8'?>");
			writeNewLine();
			
			beginElementWriter();
		}
	}
	
	
	/**
	 * Flushes all changes to file.
	 */
	public void complete() {
		// We need to call this here so that we create empty files if no records
		// are available.
		initialize();
		
		try {
			if (writer != null) {
				endElementWriter();
				
				writer.close();
			}
			
		} catch (IOException e) {
			throw new OsmosisRuntimeException("Unable to complete writing to the xml stream.", e);
		} finally {
			initialized = false;
			writer = null;
		}
	}
	
	
	/**
	 * Cleans up any open file handles.
	 */
	public void release() {
		try {
			try {
				if (writer != null) {
					writer.close();
				}
			} catch(Exception e) {
				log.log(Level.SEVERE, "Unable to close writer.", e);
			}
		} finally {
			initialized = false;
			writer = null;
		}
	}
}
