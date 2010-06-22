// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.extract.apidb.v0_6;

import java.io.File;
import java.util.Date;

import org.openstreetmap.osmosis.apidb.v0_6.ApidbChangeReader;
import org.openstreetmap.osmosis.core.OsmosisRuntimeException;
import org.openstreetmap.osmosis.core.sort.v0_6.ChangeTagSorter;
import org.openstreetmap.osmosis.extract.apidb.common.Configuration;
import org.openstreetmap.osmosis.replication.v0_6.impl.ChangesetFileNameFormatter;
import org.openstreetmap.osmosis.xml.common.CompressionMethod;
import org.openstreetmap.osmosis.xml.v0_6.XmlChangeWriter;


/**
 * Performs an extract from a database for a single time interval and writes to a change file.
 * 
 * @author Brett Henderson
 */
public class IntervalExtractor {

	private static final String TMP_FILE_NAME = "tmpchangeset.osc.gz";
	private final Configuration config;
	private final File baseDirectory;
	private final Date intervalBegin;
	private final Date intervalEnd;
	private final boolean fullHistory;


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
	 * @param fullHistory
	 *            Specifies if full version history should be returned, or just a single change per
	 *            entity for the interval.
	 */
	public IntervalExtractor(Configuration config, File baseDirectory, Date intervalBegin, Date intervalEnd,
			boolean fullHistory) {
		this.baseDirectory = baseDirectory;
		this.config = config;
		this.intervalBegin = intervalBegin;
		this.intervalEnd = intervalEnd;
		this.fullHistory = fullHistory;
	}


	/**
	 * Runs the changeset extraction.
	 */
	public void run() {
		ApidbChangeReader reader;
		XmlChangeWriter writer;
		ChangeTagSorter tagSorter;
		String fileName;
		File tmpFile;
		File file;

		// Generate the changeset file name.
		fileName = new ChangesetFileNameFormatter(config.getChangeFileBeginFormat(), config.getChangeFileEndFormat())
				.generateFileName(intervalBegin, intervalEnd);

		// Generate the temporary output file.
		tmpFile = new File(baseDirectory, TMP_FILE_NAME);

		// Generate the changeset output file.
		file = new File(baseDirectory, fileName);

		// Create the output task to write to a compressed xml file.
		writer = new XmlChangeWriter(tmpFile, CompressionMethod.GZip);

		// Create the input task to read the change interval from the database.
		reader = new ApidbChangeReader(config.getDatabaseLoginCredentials(), config.getDatabasePreferences(),
				intervalBegin, intervalEnd, fullHistory);
		
		// Create the tag sorter to ensure that output files are consistent allowing simple
		// comparisons when auditing results.
		tagSorter = new ChangeTagSorter();

		// Connect the tasks together.
		reader.setChangeSink(tagSorter);
		tagSorter.setChangeSink(writer);

		// Run the changeset extraction.
		reader.run();

		// Delete the destination file if it already exists.
		if (file.exists()) {
			if (!file.delete()) {
				throw new OsmosisRuntimeException("Unable to delete existing file " + file + ".");
			}
		}

		// Rename the temporary file to the final file name.
		if (!tmpFile.renameTo(file)) {
			throw new OsmosisRuntimeException("Unable to rename temporary file " + tmpFile + " to " + file + ".");
		}
	}
}
