// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.xml.v0_6.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;

import javax.xml.parsers.SAXParser;

import org.openstreetmap.osmosis.core.OsmosisRuntimeException;
import org.openstreetmap.osmosis.xml.common.CompressionActivator;
import org.openstreetmap.osmosis.xml.common.CompressionMethod;
import org.openstreetmap.osmosis.xml.common.SaxParserFactory;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

 /**
 * Handles common functionality used by XML readers.
 *
 * @author mcuthbert
 */
public abstract class BaseXMLReader {
    private final File file;
    private final boolean enableDateParsing;
    private final CompressionMethod method;

     /**
     * Default Constructor.
     *
     * @param file File to parse
     * @param enableDateParsing whether to enable date parsing or not
     * @param method The compression method if any
     */
    public BaseXMLReader(final File file, final boolean enableDateParsing, final CompressionMethod method) {
        this.file = file;
        this.enableDateParsing = enableDateParsing;
        this.method = method;
    }

     /**
     * Returns whether this object enables date parsing or not.
     *
     * @return true or false
     */
    public boolean isEnableDateParsing() {
        return this.enableDateParsing;
    }

     /**
     * Function to parse xml, this default function just uses the SAXParser.
     *
     * @param stream InputStream for the XML
     * @param handler A handler for the XML
     * @throws SAXException If there is any exceptions while parsing the XML
     * @throws IOException If there is any issues with the input stream
     */
    protected void parseXML(final InputStream stream, final DefaultHandler handler)
            throws SAXException, IOException {
        final SAXParser parser = SaxParserFactory.createParser();
        parser.parse(stream, handler);
    }

     /**
     * Function to handle the XML for the sub classes.
     *
     * @param handler A {@link DefaultHandler}
     */
    protected void handleXML(final DefaultHandler handler) {
        try (InputStream stream = this.getInputStream()) {
            try (InputStream compressionStream = new CompressionActivator(this.method)
                    .createCompressionInputStream(stream)) {
                this.parseXML(compressionStream, handler);
            }
        } catch (final SAXParseException e) {
            // if we get a sax parse failure, there is a good chance it may be this one:
            // Caused by: org.xml.sax.SAXParseException;
            // lineNumber: ?; columnNumber: ?; Invalid byte 2 of 4-byte UTF-8 sequence.
            // This can be solved by unzipping the contents to a temporary file and then parsing from there.
            this.unzipParse(handler);
        } catch (SAXException e) {
            throw new OsmosisRuntimeException("Unable to parse XML.", e);
        } catch (IOException e) {
            throw new OsmosisRuntimeException("Unable to read XML file " + this.file + ".", e);
        }
    }

     private void unzipParse(final DefaultHandler handler) {
        File tempFile;
        try {
            tempFile = File.createTempFile(this.getTempFilePrefix(), null);
        } catch (final IOException e) {
            throw new OsmosisRuntimeException("Failed to create temporary file.", e);
        }

         try (InputStream fis = this.getInputStream();
                GZIPInputStream gzipStream = new GZIPInputStream(fis);
                FileOutputStream fos = new FileOutputStream(tempFile)) {
            final byte[] buffer = new byte[1024];
            int length;
            while ((length = gzipStream.read(buffer)) > 0) {
                fos.write(buffer, 0, length);
            }
        } catch (IOException e) {
            throw new OsmosisRuntimeException("Unable to unzip gz file " + this.file + ".", e);
        }

         try (InputStream unzippedStream = new FileInputStream(tempFile)) {
            this.parseXML(unzippedStream, handler);
        } catch (final SAXParseException e) {
            throw new OsmosisRuntimeException(
                "Unable to parse xml file " + this.file
                        + ".  publicId=(" + e.getPublicId()
                        + "), systemId=(" + e.getSystemId()
                        + "), lineNumber=" + e.getLineNumber()
                        + ", columnNumber=" + e.getColumnNumber() + ".",
                e);
        } catch (SAXException e) {
            throw new OsmosisRuntimeException("Unable to parse XML.", e);
        } catch (IOException e) {
            throw new OsmosisRuntimeException("Unable to read XML file " + this.file + ".", e);
        }
    }

     private InputStream getInputStream() throws FileNotFoundException {
        if (this.file.getName().equals("-")) {
            return System.in;
        } else {
            return new FileInputStream(this.file);
        }
    }

     private String getTempFilePrefix() {
        return this.file.getName() + "_" + System.currentTimeMillis();
    }
}
