// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core; 

import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.openstreetmap.osmosis.core.cli.CommandLineParser;
import org.openstreetmap.osmosis.core.pipeline.common.Pipeline;


/**
 * The main entry point for the command line application.
 * 
 * @author Brett Henderson
 */
public final class Osmosis {
	private static final Logger LOG = Logger.getLogger(Osmosis.class.getName());
	
	
	/**
	 * This class cannot be instantiated.
	 */
	private Osmosis() {
	}
	
	
	/**
	 * The entry point to the application.
	 * 
	 * @param args
	 *            The command line arguments.
	 */
	public static void main(String[] args) {
		try {
			run(args);
			
			System.exit(0);
			
		} catch (Throwable t) {
			LOG.log(Level.SEVERE, "Execution aborted.", t);
		}
		
		System.exit(1);
	}
	
	
	/**
	 * This contains the real functionality of the main method. It is kept
	 * separate to allow the application to be invoked within other applications
	 * without a System.exit being called.
	 * <p>
	 * Typically an application shouldn't directly invoke this method, it should
	 * instantiate its own pipeline.
	 * 
	 * @param args
	 *            The command line arguments.
	 */
	public static void run(String[] args) {
		CommandLineParser commandLineParser;
		TaskRegistrar taskRegistrar;
		Pipeline pipeline;
		long startTime;
		long finishTime;
		
		startTime = System.currentTimeMillis();
		
		configureLoggingConsole();
		
		commandLineParser = new CommandLineParser();
		
		// Parse the command line arguments into a consumable form.
		commandLineParser.parse(args);
		
		// Configure the new logging level.
		configureLoggingLevel(commandLineParser.getLogLevelIndex());
		
		LOG.info("Osmosis Version " + OsmosisConstants.VERSION);
		taskRegistrar = new TaskRegistrar();
		taskRegistrar.initialize(commandLineParser.getPlugins());
		
		pipeline = new Pipeline(taskRegistrar.getFactoryRegister());
		
		LOG.info("Preparing pipeline.");
		pipeline.prepare(commandLineParser.getTaskInfoList());
		
		LOG.info("Launching pipeline execution.");
		pipeline.execute();
		
		LOG.info("Pipeline executing, waiting for completion.");
		pipeline.waitForCompletion();
		
		LOG.info("Pipeline complete.");
		
		finishTime = System.currentTimeMillis();
		
		LOG.info("Total execution time: " + (finishTime - startTime) + " milliseconds.");
	}
	
	
	/**
	 * Configures logging to write all output to the console.
	 */
	private static void configureLoggingConsole() {
		Logger rootLogger;
		Handler consoleHandler;
		
		rootLogger = Logger.getLogger("");
		
		// Remove any existing handlers.
		for (Handler handler : rootLogger.getHandlers()) {
			rootLogger.removeHandler(handler);
		}
		
		// Add a new console handler.
		consoleHandler = new ConsoleHandler();
		consoleHandler.setLevel(Level.ALL);
		rootLogger.addHandler(consoleHandler);
	}
	
	
	/**
	 * Configures the logging level.
	 * 
	 * @param level
	 *            The index of the logging level to apply.
	 */
	private static void configureLoggingLevel(int logLevelIndex) {
		Logger rootLogger;
		
		rootLogger = Logger.getLogger("");
		
		// Set the required logging level.
		rootLogger.setLevel(LogLevels.getLogLevel(logLevelIndex));
		
		// Set spring logging to one level lower.
		Logger.getLogger("org.springframework").setLevel(LogLevels.getLogLevel(logLevelIndex - 1));
		
		// Minimise the JPF logging.
		Logger.getLogger("org.java.plugin").setLevel(Level.WARNING);
	}
}
