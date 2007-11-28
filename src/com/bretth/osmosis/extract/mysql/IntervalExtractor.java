package com.bretth.osmosis.extract.mysql;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import com.bretth.osmosis.core.OsmosisRuntimeException;
import com.bretth.osmosis.core.database.DatabaseLoginCredentials;
import com.bretth.osmosis.core.database.DatabasePreferences;
import com.bretth.osmosis.core.mysql.v0_5.MysqlChangeReader;
import com.bretth.osmosis.core.xml.common.CompressionMethod;
import com.bretth.osmosis.core.xml.v0_5.XmlChangeWriter;


/**
 * Performs an extract from a database for a single time interval and writes to
 * a change file.
 * 
 * @author Brett Henderson
 */
public class IntervalExtractor {
	private static final String TMP_FILE_NAME = "tmpchangeset.osc.gz";
	
	private Configuration config;
	private File baseDirectory;
	private Date intervalBegin;
	private Date intervalEnd;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param config
	 *            The configuration object defining runtime parameters.
	 * @param baseDirectory
	 *            The root of the changeset extraction directory tree.
	 * @param intervalBegin
	 *            The beginning of the interval to extract.
	 * @param intervalEnd
	 *            The end of the interval to extract.
	 */
	public IntervalExtractor(Configuration config, File baseDirectory, Date intervalBegin, Date intervalEnd) {
		this.baseDirectory = baseDirectory;
		this.config = config;
		this.intervalBegin = intervalBegin;
		this.intervalEnd = intervalEnd;
	}
	
	
	/**
	 * Runs the changeset extraction.
	 */
	public void run() {
		MysqlChangeReader reader;
		XmlChangeWriter writer;
		String fileName;
		SimpleDateFormat beginDateFormat;
		SimpleDateFormat endDateFormat;
		TimeZone utcTimezone;
		File tmpFile;
		File file;
		
		beginDateFormat = new SimpleDateFormat(config.getChangeFileBeginFormat());
		endDateFormat = new SimpleDateFormat(config.getChangeFileEndFormat());
		utcTimezone = TimeZone.getTimeZone("UTC");
		beginDateFormat.setTimeZone(utcTimezone);
		endDateFormat.setTimeZone(utcTimezone);
		
		// Generate the temporary output file.
		tmpFile = new File(baseDirectory, TMP_FILE_NAME);
		
		// Generate the final file.
		fileName =
			beginDateFormat.format(intervalBegin) +
			"-" +
			endDateFormat.format(intervalEnd) +
			".osc.gz";
		file = new File(baseDirectory, fileName);
		
		// Create the output task to write to a compressed xml file.
		writer = new XmlChangeWriter(
			tmpFile,
			CompressionMethod.GZip,
			config.getEnableProductionEncodingHack()
		);
		
		// Create the input task to read the change interval from the database.
		reader = new MysqlChangeReader(
			new DatabaseLoginCredentials(
				config.getHost(),
				config.getDatabase(),
				config.getUser(),
				config.getPassword()
			),
			new DatabasePreferences(true),
			false,
			intervalBegin,
			intervalEnd
		);
		
		// Connect the input task to the output task.
		reader.setChangeSink(writer);
		
		// Run the changeset extraction.
		reader.run();
		
		// If no change file was created, create an empty one.
		if (!tmpFile.exists()) {
			try {
				if (!tmpFile.createNewFile()) {
					throw new OsmosisRuntimeException("Unable to create empty temporary file " + tmpFile + ".");
				}
			} catch (IOException e) {
				throw new OsmosisRuntimeException("Error occurred creating empty temporary file " + tmpFile + ".", e);
			}
		}
		
		// Rename the temporary file to the final file name.
		if (!tmpFile.renameTo(file)) {
			throw new OsmosisRuntimeException("Unable to rename temporary file " + tmpFile + " to " + file + ".");
		}
	}
}
