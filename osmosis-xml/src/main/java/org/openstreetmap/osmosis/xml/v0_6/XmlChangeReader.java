// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.xml.v0_6;

import org.openstreetmap.osmosis.core.task.v0_6.ChangeSink;
import org.openstreetmap.osmosis.core.task.v0_6.RunnableChangeSource;

import org.openstreetmap.osmosis.xml.common.CompressionMethod;
import org.openstreetmap.osmosis.xml.v0_6.impl.BaseXMLReader;
import org.openstreetmap.osmosis.xml.v0_6.impl.OsmChangeHandler;

import java.io.File;
import java.util.Collections;

/**
 * A change source reading from an xml file. The entire contents of the file
 * are read.
 *
 * @author Brett Henderson
 */
public class XmlChangeReader extends BaseXMLReader implements RunnableChangeSource {
	private ChangeSink changeSink;

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
	public XmlChangeReader(File file, boolean enableDateParsing, CompressionMethod compressionMethod) {
        super(file, enableDateParsing, compressionMethod);
	}

	/**
	 * {@inheritDoc}
	 */
	public void setChangeSink(ChangeSink changeSink) {
		this.changeSink = changeSink;
	}

	/**
	 * Reads all data from the file and send it to the sink.
	 */
	public void run() {
		try {
            this.changeSink.initialize(Collections.emptyMap());
            this.handleXML(new OsmChangeHandler(this.changeSink, this.isEnableDateParsing()));
            this.changeSink.complete();    
		} finally {
            this.changeSink.close();
        }
	}
}
