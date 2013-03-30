// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.pgsimple.common;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.postgis.Geometry;
import org.postgis.binary.BinaryWriter;

import org.openstreetmap.osmosis.core.OsmosisRuntimeException;
import org.openstreetmap.osmosis.core.lifecycle.Completable;


/**
 * This class provides the capability to write a file that contains data for a
 * database COPY statement for loading a single table into the database.
 * 
 * @author Brett Henderson
 */
public class CopyFileWriter implements Completable {
	
	private static Logger log = Logger.getLogger(CopyFileWriter.class.getName());
	
	
	private File file;
	private boolean initialized;
	private BufferedWriter writer;
	private boolean midRecord;
	private SimpleDateFormat dateFormat;
	private BinaryWriter postgisBinaryWriter;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param file
	 *            The file to write.
	 */
	public CopyFileWriter(File file) {
		this.file = file;
		
		midRecord = false;
		
		dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ssZ");
		postgisBinaryWriter = new BinaryWriter();
	}
	
	
	/**
	 * Adds a field separator if required.
	 * 
	 * @throws IOException
	 *             if the field cannot be written.
	 */
	private void separateField() throws IOException {
		if (midRecord) {
			writer.write('\t');
		} else {
			midRecord = true;
		}
	}
	
	
	/**
	 * Writes data to the output file.
	 * 
	 * @param data
	 *            The data to be written.
	 */
	public void writeField(boolean data) {
		initialize();
		
		try {
			separateField();
			
			if (data) {
				writer.write("t");
			} else {
				writer.write("f");
			}
			
		} catch (IOException e) {
			throw new OsmosisRuntimeException("Unable to write value (" + data + ")", e);
		}
	}
	
	
	/**
	 * Writes data to the output file.
	 * 
	 * @param data
	 *            The data to be written.
	 */
	public void writeField(int data) {
		initialize();
		
		try {
			separateField();
			
			writer.write(Integer.toString(data));
			
		} catch (IOException e) {
			throw new OsmosisRuntimeException("Unable to write value (" + data + ")", e);
		}
	}
	
	
	/**
	 * Writes data to the output file.
	 * 
	 * @param data
	 *            The data to be written.
	 */
	public void writeField(long data) {
		initialize();
		
		try {
			separateField();
			
			writer.write(Long.toString(data));
			
		} catch (IOException e) {
			throw new OsmosisRuntimeException("Unable to write value (" + data + ")", e);
		}
	}
	
	
	/**
	 * Inserts escape sequences needed to make a String suitable for writing to
	 * a COPY file.
	 * 
	 * @param data
	 *            The raw data string.
	 * @return The escaped string.
	 */
	private String escapeString(String data) {
		StringBuilder result;
		char[] dataArray;
		
		if (data == null) {
			return "\\N";
		}
		
		result = new StringBuilder(data.length());
		dataArray = data.toCharArray();
		for (int i = 0; i < dataArray.length; i++) {
			char currentChar;
			
			currentChar = dataArray[i];
			
			switch (currentChar) {
			case '\\': // Slash
				result.append("\\\\");
				break;
			case 8: // Backspace
				result.append("\\b");
				break;
			case 12: // Form feed
				result.append("\\f");
				break;
			case 10: // Newline
				result.append("\\n");
				break;
			case 13: // Carriage return
				result.append("\\r");
				break;
			case 9: // Tab
				result.append("\\t");
				break;
			case 11: // Vertical tab
				result.append("\\v");
				break;
			default:
				result.append(currentChar);
				
			}
		}
		
		return result.toString();
	}
	
	
	/**
	 * Writes data to the output file.
	 * 
	 * @param data
	 *            The data to be written.
	 */
	public void writeField(String data) {
		initialize();
		
		try {
			separateField();
			
			writer.write(escapeString(data));
			
		} catch (IOException e) {
			throw new OsmosisRuntimeException("Unable to write value (" + data + ")", e);
		}
	}
	
	
	/**
	 * Writes data to the output file.
	 * 
	 * @param data
	 *            The data to be written.
	 */
	public void writeField(Date data) {
		initialize();
		
		try {
			separateField();
			
			writer.write(dateFormat.format(data));
			
		} catch (IOException e) {
			throw new OsmosisRuntimeException("Unable to write value (" + data + ")", e);
		}
	}
	
	
	/**
	 * Writes data to the output file.
	 * 
	 * @param data
	 *            The data to be written.
	 */
	public void writeField(Geometry data) {
		initialize();
		
		try {
			separateField();

		    if (data == null) {
                writer.write(escapeString(null));
            } else {	
			    writer.write(postgisBinaryWriter.writeHexed(data));
            }
			
		} catch (IOException e) {
			throw new OsmosisRuntimeException("Unable to write value (" + data + ")", e);
		}
	}
	
	
	/**
	 * Writes a new line in the output file.
	 */
	public void endRecord() {
		try {
			writer.newLine();
			midRecord = false;
			
		} catch (IOException e) {
			throw new OsmosisRuntimeException("Unable to end record.", e);
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
			OutputStream outStream = null;
			
			try {
				outStream = new FileOutputStream(file);
				
				writer = new BufferedWriter(
						new OutputStreamWriter(new BufferedOutputStream(outStream, 65536), "UTF-8"));
				
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
			
			initialized = true;
		}
	}
	
	
	/**
	 * Flushes all changes to file.
	 */
	public void complete() {
		initialize();
		
		try {
			if (midRecord) {
				throw new OsmosisRuntimeException("The current record has not been ended.");
			}
			
			if (writer != null) {
				writer.close();
			}
			
		} catch (IOException e) {
			throw new OsmosisRuntimeException("Unable to complete writing to the data stream.", e);
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
			} catch (IOException e) {
				log.log(Level.SEVERE, "Unable to close writer.", e);
			}
			
		} finally {
			initialized = false;
			writer = null;
		}
	}
}
