package com.bretth.osmosis.core.xml.v0_6;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.stream.XMLInputFactory;

import org.codehaus.stax2.XMLInputFactory2;
import org.codehaus.stax2.XMLStreamReader2;

import com.bretth.osmosis.core.OsmosisRuntimeException;
import com.bretth.osmosis.core.task.v0_6.RunnableSource;
import com.bretth.osmosis.core.task.v0_6.Sink;
import com.bretth.osmosis.core.xml.common.CompressionActivator;
import com.bretth.osmosis.core.xml.common.CompressionMethod;
import com.bretth.osmosis.core.xml.v0_6.impl.FastXmlParser;


/**
 * An OSM data source reading from an xml file. The entire contents of the file
 * are read.
 * 
 * @author Jiri Clement
 * @author Brett Henderson
 */
public class FastXmlReader implements RunnableSource {
		
		private static Logger log = Logger.getLogger(FastXmlReader.class.getName());
		
		private Sink sink;
		private final File file;
		private final boolean enableDateParsing;
		private final CompressionMethod compressionMethod;
		
		
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
		public FastXmlReader(File file, boolean enableDateParsing, CompressionMethod compressionMethod) {
			this.file = file;
			this.enableDateParsing = enableDateParsing;
			this.compressionMethod = compressionMethod;
		}
		
		
		/**
		 * {@inheritDoc}
		 */
		public void setSink(Sink sink) {
			this.sink = sink;
		}
		
				
		
		/**
		 * Reads all data from the file and send it to the sink.
		 */
		public void run() {
			InputStream inputStream = null;
			FastXmlParser parser = null;
			
			try {
				
				// make "-" an alias for /dev/stdin
				if(file.getName().equals("-"))
				{
					inputStream = System.in;
				} else {
					inputStream = new FileInputStream(file);
				}
				
				
				inputStream =
					new CompressionActivator(compressionMethod).
						createCompressionInputStream(inputStream);
				
		        XMLInputFactory factory = XMLInputFactory2.newInstance();
		        factory.setProperty(XMLInputFactory2.IS_COALESCING, false);
		        factory.setProperty(XMLInputFactory2.IS_NAMESPACE_AWARE, false);
		        factory.setProperty(XMLInputFactory2.IS_VALIDATING, false);
		        XMLStreamReader2 xpp = (XMLStreamReader2)factory.createXMLStreamReader(inputStream);
				
				parser = new FastXmlParser(sink, xpp, enableDateParsing);
				parser.readOsm();
				
				sink.complete();
				
			} catch (Exception e) {
				throw new OsmosisRuntimeException("Unable to read XML file " + file + ".", e);
			} finally {
				sink.release();
				
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
