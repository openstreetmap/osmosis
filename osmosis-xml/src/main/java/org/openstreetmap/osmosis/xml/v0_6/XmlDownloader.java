// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.xml.v0_6;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import org.openstreetmap.osmosis.core.OsmosisRuntimeException;
import org.openstreetmap.osmosis.core.container.v0_6.BoundContainer;
import org.openstreetmap.osmosis.core.domain.v0_6.Bound;
import org.openstreetmap.osmosis.core.task.v0_6.RunnableSource;
import org.openstreetmap.osmosis.core.task.v0_6.Sink;
import org.openstreetmap.osmosis.core.util.MultiMemberGZIPInputStream;
import org.openstreetmap.osmosis.xml.v0_6.impl.OsmHandler;
import org.openstreetmap.osmosis.xml.v0_6.impl.XmlConstants;


/**
 * An OSM data source reading from an osm-xml file from the
 * OpenStreetMap-server.
 *
 * @author <a href="mailto:Marcus@Wolschon.biz">Marcus Wolschon</a>
 */
public class XmlDownloader implements RunnableSource {

    /**
     * The http-response-code for OK.
     */
    private static final int RESPONSECODE_OK = 200;


    /**
     * My logger for debug- and error-output.
     */
    private static Logger log = Logger.getLogger(XmlDownloader.class.getName());


    /**
     * The timeout we use for the  HttpURLConnection.
     */
    private static final int TIMEOUT = 15000;


    /**
     * Where to deliver the loaded data.
     */
    private Sink mySink;

    /**
     * Left longitude of the bounding box.
     */
    private double myLeft;

    /**
     * Right longitude of the bounding box.
     */
    private double myRight;

    /**
     * Top latitude of the bounding box.
     */
    private double myTop;

    /**
     * Bottom latitude of the bounding box.
     */
    private double myBottom;

    /**
     * The base url of the server.
     * Defaults to. "http://www.openstreetmap.org/api/0.5".
     */
    private String myBaseUrl = XmlConstants.DEFAULT_URL;

    /**
     * The http connection used to retrieve data.
     */
    private HttpURLConnection myActiveConnection;

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
     *            (optional) The base url of the server (eg.
     *            http://www.openstreetmap.org/api/0.5).
     */
    public XmlDownloader(final double left,
                         final double right,
                         final double top,
                         final double bottom,
                         final String baseUrl) {
        this.myLeft = Math.min(left, right);
        this.myRight = Math.max(left, right);
        this.myTop = Math.max(top, bottom);
        this.myBottom = Math.min(top, bottom);
        if (baseUrl != null) {
            this.myBaseUrl = baseUrl;
        }
    }


    /**
     * {@inheritDoc}
     */
    public void setSink(final Sink aSink) {
        this.mySink = aSink;
    }

    /**
     * Cleans up any resources remaining after completion.
     */
    private void cleanup() {
        if (myActiveConnection != null) {
            try {
                myActiveConnection.disconnect();
            } catch (Exception e) {
                log.log(Level.SEVERE, "Unable to disconnect.", e);
            }
            myActiveConnection = null;
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
     * Reads all data from the server and send it to the {@link Sink}.
     */
    public void run() {
        try {
        	mySink.initialize(Collections.<String, Object>emptyMap());
        	
            SAXParser parser = createParser();
            InputStream inputStream =
            	getInputStream(myBaseUrl + "/map?bbox=" + myLeft + "," + myBottom + "," + myRight + "," + myTop);

            // First send the Bound down the pipeline
            mySink.process(new BoundContainer(new Bound(myRight, myLeft, myTop, myBottom, myBaseUrl)));

            try {
                parser.parse(inputStream, new OsmHandler(mySink, true));
            } finally {
                inputStream.close();
                inputStream = null;
            }

            mySink.complete();

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
            mySink.release();

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
    private InputStream getInputStream(final String pUrlStr) throws IOException {
        URL url;
        int responseCode;
        String encoding;

        url = new URL(pUrlStr);
        myActiveConnection = (HttpURLConnection) url.openConnection();

        myActiveConnection.setRequestProperty("Accept-Encoding", "gzip, deflate");

        responseCode = myActiveConnection.getResponseCode();

        if (responseCode != RESPONSECODE_OK) {
            String message;
            String apiErrorMessage;

            apiErrorMessage = myActiveConnection.getHeaderField("Error");

            if (apiErrorMessage != null) {
                message = "Received API HTTP response code " + responseCode
                + " with message \"" + apiErrorMessage
                + "\" for URL \"" + pUrlStr + "\".";
            } else {
                message = "Received API HTTP response code " + responseCode
                + " for URL \"" + pUrlStr + "\".";
            }

            throw new OsmosisRuntimeException(message);
        }

        myActiveConnection.setConnectTimeout(TIMEOUT);

        encoding = myActiveConnection.getContentEncoding();

        responseStream = myActiveConnection.getInputStream();
        if (encoding != null && encoding.equalsIgnoreCase("gzip")) {
            responseStream = new MultiMemberGZIPInputStream(responseStream);
        } else if (encoding != null && encoding.equalsIgnoreCase("deflate")) {
            responseStream = new InflaterInputStream(responseStream, new Inflater(true));
        }

        return responseStream;
    }
}
