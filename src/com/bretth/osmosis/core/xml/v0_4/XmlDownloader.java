package com.bretth.osmosis.core.xml.v0_4;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import com.bretth.osmosis.core.OsmosisRuntimeException;
import com.bretth.osmosis.core.task.v0_4.RunnableSource;
import com.bretth.osmosis.core.task.v0_4.Sink;
import com.bretth.osmosis.core.xml.v0_4.impl.OsmHandler;


/**
 * An OSM data source reading from an osm-xml file from the
 * OpenStreetMap-server.
 * 
 * @author <a href="mailto:Marcus@Wolschon.biz">Marcus Wolschon</a>
 */
public class XmlDownloader implements RunnableSource {
	
	private static Logger log = Logger.getLogger(XmlDownloader.class.getName());
	
	
    /**
     * The timeout we use for the  HttpURLConnection.
     */
    private static final int TIMEOUT = 15000;
    
    
    /**
     * Where to deliver the loaded data.
     */
    private Sink sink;
    
    /**
     * Left longitude of the bounding box.
     */
    private double left;
    
    /**
     * Right longitude of the bounding box.
     */
    private double right;
    
    /**
     * Top latitude of the bounding box.
     */
    private double top;
    
    /**
     * Bottom latitude of the bounding box.
     */
    private double bottom;
    
    /**
     * The base url of the server (eg. http://www.openstreetmap.org/api/0.4).
     */
    private String baseUrl;
    
    
    /**
     * The http connection used to retrieve data.
     */
    private HttpURLConnection activeConnection;
    
    
    /**
     * The stream providing response data.
     */
    private InputStream responseStream;
    
    
    /**
	 * Creates a new instance with the specified geographical coordinates.
	 * 
	 * @param left
	 *            The longitude marking the left edge of the bounding box.
	 * @param right
	 *            The longitude marking the right edge of the bounding box.
	 * @param top
	 *            The latitude marking the top edge of the bounding box.
	 * @param bottom
	 *            The latitude marking the bottom edge of the bounding box.
	 * @param baseUrl
	 *            The base url of the server (eg.
	 *            http://www.openstreetmap.org/api/0.4).
	 */
    public XmlDownloader(double left,
                         double right,
                         double top,
                         double bottom,
                         String baseUrl) {
        this.left = left;
        this.right = right;
        this.top = top;
        this.bottom = bottom;
        this.baseUrl = baseUrl;
    }


    /**
     * {@inheritDoc}
     */
    public void setSink(final Sink aSink) {
        this.sink = aSink;
    }
    
    
    /**
     * Cleans up any resources remaining after completion.
     */
    private void cleanup() {
    	if (activeConnection != null) {
    		activeConnection.disconnect();
    		activeConnection = null;
    	}
    	
    	if (responseStream != null) {
			try {
				responseStream.close();
			} catch (IOException e) {
				log.log(Level.SEVERE, "Unable to close response stream.", e);
			}
			responseStream = null;
		}
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
        	InputStream inputStream;
            SAXParser parser;
            
            parser = createParser();
            
            inputStream = getInputStream(baseUrl + "/map?bbox=" + left + "," + bottom + "," + right + "," + top);
            
            parser.parse(inputStream, new OsmHandler(sink, true));
            
            inputStream.close();
            inputStream = null;
            
            sink.complete();
            
        } catch (SAXParseException e) {
			throw new OsmosisRuntimeException(
				"Unable to parse xml"
				+ ".  publicId=(" + e.getPublicId()
				+ "), systemId=(" + e.getSystemId()
				+ "), lineNumber=" + e.getLineNumber()
				+ ", columnNumber=" + e.getColumnNumber() + ".",
				e);
		} catch (SAXException e) {
            throw new OsmosisRuntimeException("Unable to parse XML.", e);
        } catch (IOException e) {
            throw new OsmosisRuntimeException("Unable to read XML.", e);
        } finally {
            sink.release();
            
            cleanup();
        }
    }
    
    
    /**
	 * Open a connection to the given url and return a reader on the input
	 * stream from that connection.
	 * 
	 * @param pUrlStr
	 *            The exact url to connect to.
	 * @return An reader reading the input stream (servers answer) or
	 *         <code>null</code>.
	 * @throws IOException
	 *             on io-errors
	 */
	private InputStream getInputStream(String urlStr) throws IOException {
        URL url;
        int responseCode;
        String encoding;
        
        url = new URL(urlStr);
        activeConnection = (HttpURLConnection) url.openConnection();
        
        activeConnection.setRequestProperty("Accept-Encoding", "gzip, deflate");
        
        responseCode = activeConnection.getResponseCode();
        
        if (responseCode != 200) {
        	throw new OsmosisRuntimeException("Received API HTTP response code " + responseCode + ".");
        }
        
        activeConnection.setConnectTimeout(TIMEOUT);
        
        encoding = activeConnection.getContentEncoding();
        
        responseStream = activeConnection.getInputStream();
        if (encoding != null && encoding.equalsIgnoreCase("gzip")) {
        	responseStream = new GZIPInputStream(responseStream);
        } else if (encoding != null && encoding.equalsIgnoreCase("deflate")) {
        	responseStream = new InflaterInputStream(responseStream, new Inflater(true));
        }
        
        return responseStream;
    }
}
