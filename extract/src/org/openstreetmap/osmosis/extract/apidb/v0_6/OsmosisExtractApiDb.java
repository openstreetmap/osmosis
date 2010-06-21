// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.extract.apidb.v0_6;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import org.openstreetmap.osmosis.core.OsmosisConstants;
import org.openstreetmap.osmosis.core.OsmosisRuntimeException;
import org.openstreetmap.osmosis.core.util.FileBasedLock;
import org.openstreetmap.osmosis.core.util.ResourceFileManager;
import org.openstreetmap.osmosis.extract.apidb.common.Configuration;
import org.openstreetmap.osmosis.replication.common.TimestampTracker;


/**
 * The main entry point for the apidb change extraction application.
 * 
 * @author Brett Henderson
 */
public class OsmosisExtractApiDb {

	private static final File LOCK_FILE = new File("osmosis-extract-apidb.lock");
	private static final File CONFIG_FILE = new File("osmosis-extract-apidb.conf");
	private static final File DATA_DIR = new File("data");
	private static final File TSTAMP_FILE = new File("timestamp.txt");
	private static final File TSTAMP_NEW_FILE = new File("timestampnew.txt");
	private static final File DATA_TSTAMP_FILE = new File("data/timestamp.txt");
	private static final File DATA_TSTAMP_NEW_FILE = new File("data/timestampnew.txt");
	private static final String CONFIG_RESOURCE = "osmosis-extract-apidb.conf";
	private static final String COMMAND_HELP = "help";
	private static final String COMMAND_INITIALIZE = "initialize";
	private static final String COMMAND_INFO = "info";
	private static final String COMMAND_EXTRACT = "extract";
	private static final String COMMAND_LINE_DATE_FORMAT = "yyyy-MM-dd_HH:mm:ss";
	private static final Locale COMMAND_LINE_DATE_LOCALE = Locale.US;
	private static final TimeZone COMMAND_LINE_DATE_TIMEZONE = TimeZone.getTimeZone("UTC");
	private final String[] programArgs;


	/**
	 * The entry point to the application.
	 * 
	 * @param args
	 *            The command line arguments.
	 */
	public static void main(String[] args) {
		FileBasedLock fileLock = new FileBasedLock(LOCK_FILE);
		boolean success = false;

		try {
			fileLock.lock();

			new OsmosisExtractApiDb(args).run();

			fileLock.unlock();

			success = true;

		} finally {
			fileLock.release();
		}

		// Indicate success or otherwise.
		if (success) {
			System.exit(0);
		} else {
			System.exit(1);
		}
	}


	/**
	 * Creates a new instance.
	 * 
	 * @param programArgs
	 *            The command line arguments.
	 */
	public OsmosisExtractApiDb(String[] programArgs) {
		this.programArgs = programArgs;
	}


	/**
	 * Launches program execution.
	 */
	public void run() {
		int argIndex;
		String command;

		if (programArgs.length == 0) {
			helpCommand();
		} else {
			argIndex = 0;
			command = programArgs[argIndex++];

			if (COMMAND_HELP.equals(command)) {
				helpCommand();
			} else if (COMMAND_INITIALIZE.equals(command)) {
				initializeCommand(programArgs, argIndex);
			} else if (COMMAND_INFO.equals(command)) {
				infoCommand();
			} else if (COMMAND_EXTRACT.equals(command)) {
				extractCommand();
			} else {
				System.out.println("Command " + command + " is not recognised.");
			}
		}
	}


	/**
	 * Creates a configuration object.
	 * 
	 * @return The configuration.
	 */
	private Configuration getConfiguration() {
		return new Configuration(CONFIG_FILE);
	}


	/**
	 * Creates a timestamp tracker object for persisting the currently extracted timestamp.
	 * 
	 * @return The timestamp tracker.
	 */
	private TimestampTracker getTimestampTracker() {
		return new TimestampTracker(TSTAMP_FILE, TSTAMP_NEW_FILE);
	}


	/**
	 * Creates a timestamp tracker object for persisting the currently extracted timestamp into the
	 * data directory for consumers to download.
	 * 
	 * @return The timestamp tracker.
	 */
	private TimestampTracker getDataTimestampSetter() {
		return new TimestampTracker(DATA_TSTAMP_FILE, DATA_TSTAMP_NEW_FILE);
	}


	/**
	 * Gets a date argument from the program arguments.
	 * 
	 * @param args
	 *            The program arguments.
	 * @param argIndex
	 *            The current argument index.
	 * @return The parsed date.
	 */
	private Date getDateArgument(String[] args, int argIndex) {
		// Verify that the argument is available.
		if (args.length <= argIndex) {
			throw new OsmosisRuntimeException("A date argument is required at argument " + (argIndex + 1) + ".");
		}

		try {
			SimpleDateFormat dateFormat;

			dateFormat = new SimpleDateFormat(COMMAND_LINE_DATE_FORMAT, COMMAND_LINE_DATE_LOCALE);
			dateFormat.setTimeZone(COMMAND_LINE_DATE_TIMEZONE);

			return dateFormat.parse(args[argIndex]);

		} catch (ParseException e) {
			throw new OsmosisRuntimeException("Argument " + (argIndex + 1) + " must be a date in format "
					+ COMMAND_LINE_DATE_FORMAT + ".", e);
		}
	}


	/**
	 * Prints usage information to the console.
	 */
	private void helpCommand() {
		System.out.println("Osmosis Extract ApiDb Version " + OsmosisConstants.VERSION);
		System.out.println("Usage: osmosis-apidb-extract <command> <options>");
		System.out.println("Commands:");
		System.out.println("\t" + COMMAND_INITIALIZE + " <" + COMMAND_LINE_DATE_FORMAT + ">");
		System.out.println("\t" + COMMAND_INFO);
		System.out.println("\t" + COMMAND_EXTRACT);
	}


	/**
	 * Initialises the current working directory.
	 * 
	 * @param args
	 *            The input arguments.
	 * @param initialArgIndex
	 *            The current offset into the arguments.
	 */
	private void initializeCommand(String[] args, int initialArgIndex) {
		int currentArgIndex;
		Date initialExtractDate;
		ResourceFileManager resourceFileManager;

		// Get the command line arguments.
		currentArgIndex = initialArgIndex;
		initialExtractDate = getDateArgument(args, currentArgIndex++);

		if (CONFIG_FILE.exists()) {
			throw new OsmosisRuntimeException("Config file " + CONFIG_FILE + " already exists.");
		}
		resourceFileManager = new ResourceFileManager();
		resourceFileManager.copyResourceToFile(getClass(), CONFIG_RESOURCE, CONFIG_FILE);

		if (!DATA_DIR.exists()) {
			if (!DATA_DIR.mkdir()) {
				throw new OsmosisRuntimeException("Unable to create directory " + DATA_DIR);
			}
		}

		if (TSTAMP_FILE.exists()) {
			throw new OsmosisRuntimeException("Extract timestamp file " + TSTAMP_FILE + " already exists.");
		}
		getTimestampTracker().setTime(initialExtractDate);
	}


	/**
	 * Provides information about the state of the current working directory.
	 */
	private void infoCommand() {
		Configuration configuration;
		TimestampTracker timestampTracker;

		configuration = getConfiguration();
		timestampTracker = getTimestampTracker();

		System.out.println("Configuration");
		System.out.println("\thost: " + configuration.getHost());
		System.out.println("\tdatabase: " + configuration.getDatabase());
		System.out.println("\tuser: " + configuration.getUser());
		System.out.println("\tpassword: " + configuration.getPassword());
		System.out.println("\tdb: " + configuration.getDbType());
		System.out.println("\tintervalLength: " + configuration.getIntervalLength());
		System.out.println("\tlagLength: " + configuration.getLagLength());
		System.out.println("\tchangeSetBeginFormat: " + configuration.getChangeFileBeginFormat());
		System.out.println("\tchangeSetEndFormat: " + configuration.getChangeFileEndFormat());
		System.out.println();
		System.out.println("Data");
		System.out.println("\tCurrent Timestamp: " + timestampTracker.getTime());
	}


	/**
	 * Performs the extraction process.
	 */
	private void extractCommand() {
		Configuration configuration;
		DatabaseTimeLoader timeLoader;
		boolean fullHistory;
		TimestampTracker timestampTracker;
		TimestampTracker dataTimestampSetter;
		long extractTime;
		long maximumExtractTime;
		long nextExtractTime;

		configuration = getConfiguration();
		timeLoader = new DatabaseTimeLoader(configuration.getDatabaseLoginCredentials());
		fullHistory = configuration.getReadFullHistory();
		timestampTracker = getTimestampTracker();
		dataTimestampSetter = getDataTimestampSetter();

		// Determine the last extraction time.
		extractTime = timestampTracker.getTime().getTime();

		while (true) {
			Date intervalBegin;
			Date intervalEnd;
			IntervalExtractor extractor;

			nextExtractTime = extractTime + configuration.getIntervalLength();

			// Determine the maximum extraction time. It is the current time minus the lag length.
			maximumExtractTime = timeLoader.getDatabaseTime().getTime() - configuration.getLagLength();

			// Stop when the maximum extraction time is passed.
			if (nextExtractTime > maximumExtractTime) {
				break;
			}

			// Calculate the beginning and end of the next changeset interval.
			intervalBegin = new Date(extractTime);
			intervalEnd = new Date(nextExtractTime);

			// Extract a changeset for the current interval.
			extractor = new IntervalExtractor(configuration, DATA_DIR, intervalBegin, intervalEnd, fullHistory);
			extractor.run();

			// Update and persist the latest extract timestamp to both the
			// working directory and the output data directory.
			extractTime = nextExtractTime;
			timestampTracker.setTime(new Date(extractTime));
			dataTimestampSetter.setTime(new Date(extractTime));
		}
	}
}
