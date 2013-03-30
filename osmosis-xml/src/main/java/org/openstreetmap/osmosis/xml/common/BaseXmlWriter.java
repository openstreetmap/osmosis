// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.xml.common;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Map;
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
	
	
	private boolean closeRequired;
	private boolean writerProvided;
	private File file;
	private boolean initialized;
	private BufferedWriter writer;
	private CompressionMethod compressionMethod;
	
	
	/**
	 * Creates a new instance to write to the provided writer.
	 * 
	 * @param writer The writer to receive data.  This writer will not be closed on completion.
	 */
	public BaseXmlWriter(BufferedWriter writer) {
		this.writer = writer;
		
		writerProvided = true;
		closeRequired = false;
	}
	
	
	/**
	 * Creates a new instance to write to the specified file.
	 * 
	 * @param file
	 *            The file to write.
	 * @param compressionMethod
	 *            Specifies the compression method to employ.
	 */
	public BaseXmlWriter(File file, CompressionMethod compressionMethod) {
		this.file = file;
		this.compressionMethod = compressionMethod;
		
		writerProvided = false;
		closeRequired = true;
	}
	
	
	/**
	 * Sets the writer on the element writer used for this implementation.
	 * 
	 * @param resultWriter
	 *            The writer receiving xml data.
	 */
	protected abstract void setWriterOnElementWriter(BufferedWriter resultWriter);
	
	
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
     * {@inheritDoc}
     */
    public void initialize(Map<String, Object> metaData) {
		// Do nothing.
	}
	
	
	/**
	 * Initialises the output file for writing. This must be called by
	 * sub-classes before any writing is performed. This method may be called
	 * multiple times without adverse affect allowing sub-classes to invoke it
	 * every time they perform processing.
	 */
	protected void initialize() {
		if (!initialized) {
			if (!writerProvided) {
				OutputStream outStream = null;
				
				try {
					OutputStreamWriter outStreamWriter;
					
					// make "-" an alias for /dev/stdout
					if (file.getName().equals("-")) {
						outStream = System.out;
						
						// We don't want to close stdout because we'll need to
						// re-use it if we receive multiple streams.
						closeRequired = false;
					} else {
						outStream = new FileOutputStream(file);
					}
					
					outStream =
						new CompressionActivator(compressionMethod).createCompressionOutputStream(outStream);
					
					outStreamWriter = new OutputStreamWriter(outStream, "UTF-8");
					
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

		endElementWriter();

		try {
			if (closeRequired) {
				writer.close();
				writer = null;
			} else if (!writerProvided) {
				writer.flush();
			}

			initialized = false;

		} catch (IOException e) {
			throw new OsmosisRuntimeException("Unable to complete writing to the xml stream.", e);
		}
	}
	
	
	/**
	 * Cleans up any open file handles.
	 */
	public void release() {
		try {
			if (closeRequired) {
				try {
					try {
						if (writer != null) {
							writer.close();
						}
					} catch (IOException e) {
						log.log(Level.SEVERE, "Unable to close writer.", e);
					}
				} finally {
					writer = null;
				}
			}
		} finally {
			initialized = false;
		}
	}
}
