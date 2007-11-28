package com.bretth.osmosis.extract.mysql;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
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
	private static final File TSTAMP_FILE = new File("var/timestamp.txt");
	private static final File TSTAMP_TMP_FILE = new File("var/timestamptmp.txt");
	private static final File DATA_DIR = new File("data");
	
	private static final String CONFIG_RESOURCE = "osmosis-extract-mysql.conf";
	
	private static final String COMMAND_HELP = "help";
	private static final String COMMAND_INITIALIZE = "initialize";
	private static final String COMMAND_SUMMARIZE = "summarize";
	private static final String COMMAND_EXTRACT = "extract";
	
	
	private String[] programArgs;
	
	
	/**
	 * The entry point to the application.
	 * 
	 * @param args
	 *            The command line arguments.
	 */
	public static void main(String[] args) {
		FileBasedLock fileLock = new FileBasedLock(LOCK_FILE);
		
		try {
			fileLock.lock();
			
			new OsmosisExtractMysql(args).run();
			
			fileLock.release();
			
		} finally {
			fileLock.release();
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
			} else if (COMMAND_SUMMARIZE.equals(command)) {
				summarizeCommand();
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
	
	
	private TimestampTracker getTimestampTracker() {
		return new TimestampTracker(TSTAMP_FILE, TSTAMP_TMP_FILE);
	}
	
	
	/**
	 * Prints usage information to the console.
	 */
	private void helpCommand() {
		System.out.println("Osmosis Extract MySQL Version " + OsmosisConstants.VERSION);
		System.out.println("Usage: osmosis-mysql-extract <command> <options>");
		System.out.println("Commands:");
		System.out.println("\t" + COMMAND_INITIALIZE + " <yyyyMMdd_HH:mm:ss>");
		System.out.println("\t" + COMMAND_SUMMARIZE);
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
	 * @param argIndex
	 *            The current offset into the arguments.
	 */
	private void initializeCommand(String args[], int argIndex) {
		if (CONFIG_FILE.exists()) {
			throw new OsmosisRuntimeException("Config file " + CONFIG_FILE + " already exists.");
		} else {
			copyResourceToFile(CONFIG_RESOURCE, CONFIG_FILE);
		}
		
		if (DATA_DIR.exists()) {
			throw new OsmosisRuntimeException("Directory " + DATA_DIR + " already exists.");
		} else {
			if (!DATA_DIR.mkdir()) {
				throw new OsmosisRuntimeException("Unable to create directory " + DATA_DIR);
			}
		}
	}
	
	
	/**
	 * Summarises the state of the current working directory.
	 */
	private void summarizeCommand() {
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
			extractor = new IntervalExtractor(configuration, new File("./"), intervalBegin, intervalEnd);
			extractor.run();
			
			// Update and persist the latest extract timestamp.
			extractTime = nextExtractTime;
			timestampTracker.setTime(new Date(extractTime));
			
		}
	}
}
