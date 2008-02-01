// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package com.bretth.osmosis.extract.mysql;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.logging.Logger;

import com.bretth.osmosis.core.OsmosisConstants;
import com.bretth.osmosis.core.OsmosisRuntimeException;


/**
 * The main entry point for the mysql change extraction application.
 * 
 * @author Brett Henderson
 */
public class OsmosisExtractMysql {
	
	private static final Logger log = Logger.getLogger(OsmosisExtractMysql.class.getName());
	
	
	private static final File LOCK_FILE = new File("osmosis-extract-mysql.lock");
	private static final File CONFIG_FILE = new File("osmosis-extract-mysql.conf");
	private static final File TSTAMP_FILE = new File("timestamp.txt");
	private static final File TSTAMP_NEW_FILE = new File("timestampnew.txt");
	private static final File DATA_DIR = new File("data");
	
	private static final String CONFIG_RESOURCE = "osmosis-extract-mysql.conf";
	
	private static final String COMMAND_HELP = "help";
	private static final String COMMAND_INITIALIZE = "initialize";
	private static final String COMMAND_INFO = "info";
	private static final String COMMAND_EXTRACT = "extract";
	private static final String COMMAND_LINE_DATE_FORMAT = "yyyy-MM-dd_HH:mm:ss";
	private static final Locale COMMAND_LINE_DATE_LOCALE = Locale.US;
	private static final TimeZone COMMAND_LINE_DATE_TIMEZONE = TimeZone.getTimeZone("UTC");
	
	
	private String[] programArgs;
	
	
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
			
			new OsmosisExtractMysql(args).run();
			
			fileLock.release();
			
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
	public OsmosisExtractMysql(String[] programArgs) {
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
	 * Creates a timestamp tracker object.
	 * 
	 * @return The timestamp tracker.
	 */
	private TimestampTracker getTimestampTracker() {
		return new TimestampTracker(TSTAMP_FILE, TSTAMP_NEW_FILE);
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
	private Date getDateArgument(String args[], int argIndex) {
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
			throw new OsmosisRuntimeException(
				"Argument " + (argIndex + 1) + " must be a date in format " + COMMAND_LINE_DATE_FORMAT + ".", e);
		}
	}
	
	
	/**
	 * Prints usage information to the console.
	 */
	private void helpCommand() {
		System.out.println("Osmosis Extract MySQL Version " + OsmosisConstants.VERSION);
		System.out.println("Usage: osmosis-mysql-extract <command> <options>");
		System.out.println("Commands:");
		System.out.println("\t" + COMMAND_INITIALIZE + " <" + COMMAND_LINE_DATE_FORMAT + ">");
		System.out.println("\t" + COMMAND_INFO);
		System.out.println("\t" + COMMAND_EXTRACT);
	}
	
	
	/**
	 * Copies a packaged resource to a file on the file system.
	 * 
	 * @param sourceResource
	 *            The input resource.
	 * @param destinationFile
	 *            The output file.
	 */
	private void copyResourceToFile(String sourceResource, File destinationFile) {
		InputStream is = null;
		OutputStream os = null;
		
		try {
			byte buffer[];
			int bytesRead;
			
			buffer = new byte[4096];
			
			is = getClass().getResourceAsStream(sourceResource);
			os = new FileOutputStream(destinationFile);
			
			while (true) {
				bytesRead = is.read(buffer);
				
				// Stop reading if no more data is available.
				if (bytesRead < 0) {
					break;
				}
				
				os.write(buffer, 0, bytesRead);
			}
			
			is.close();
			os.close();
			
		} catch (IOException e) {
			throw new OsmosisRuntimeException("Unable to copy resource " + sourceResource + " to file " + destinationFile);
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (Exception e) {
					log.warning("Unable to close input stream for resource " + sourceResource);
				}
			}
			if (os != null) {
				try {
					os.close();
				} catch (Exception e) {
					log.warning("Unable to close output stream for file " + destinationFile);
				}
			}
		}
	}
	
	
	/**
	 * Initialises the current working directory.
	 * 
	 * @param args
	 *            The input arguments.
	 * @param initialArgIndex
	 *            The current offset into the arguments.
	 */
	private void initializeCommand(String args[], int initialArgIndex) {
		int currentArgIndex;
		Date initialExtractDate;
		
		// Get the command line arguments.
		currentArgIndex = initialArgIndex;
		initialExtractDate = getDateArgument(args, currentArgIndex++);
		
		if (CONFIG_FILE.exists()) {
			throw new OsmosisRuntimeException("Config file " + CONFIG_FILE + " already exists.");
		}
		copyResourceToFile(CONFIG_RESOURCE, CONFIG_FILE);
		
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
		TimestampTracker timestampTracker;
		long extractTime;
		long maximumExtractTime;
		long nextExtractTime;
		
		configuration = getConfiguration();
		timestampTracker = getTimestampTracker();
		
		// Determine the last extraction time.
		extractTime = timestampTracker.getTime().getTime();
		
		// Determine the maximum extraction time.  It is the current time minus the lag length.
		maximumExtractTime = new Date().getTime() - configuration.getLagLength();
		
		while (true) {
			Date intervalBegin;
			Date intervalEnd;
			IntervalExtractor extractor;
			
			nextExtractTime = extractTime + configuration.getIntervalLength();
			
			// Stop when the maximum extraction time is passed.
			if (nextExtractTime > maximumExtractTime) {
				break;
			}
			
			// Calculate the beginning and end of the next changeset interval.
			intervalBegin = new Date(extractTime);
			intervalEnd = new Date(nextExtractTime);
			
			// Extract a changeset for the current interval.
			extractor = new IntervalExtractor(configuration, DATA_DIR, intervalBegin, intervalEnd);
			extractor.run();
			
			// Update and persist the latest extract timestamp.
			extractTime = nextExtractTime;
			timestampTracker.setTime(new Date(extractTime));
			
		}
	}
}
