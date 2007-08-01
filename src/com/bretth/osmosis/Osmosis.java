package com.bretth.osmosis; 

import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.bretth.osmosis.pipeline.Pipeline;


/**
 * The main entry point for the application.
 * 
 * @author Brett Henderson
 */
public class Osmosis {
	private static final String VERSION = "0.5.1";
	
	private static final Logger log = Logger.getLogger(Osmosis.class.getName());
	
	
	/**
	 * The entry point to the application.
	 * 
	 * @param args The command line arguments.
	 */
	public static void main(String[] args) {
		try {
			Pipeline pipeline;
			
			initializeLogging();
			
			log.info("Osmosis Version " + VERSION);
			TaskRegistrar.initialize();
			
			pipeline = new Pipeline();
			
			log.fine("Preparing pipeline.");
			pipeline.prepare(args);
			
			log.fine("Executing pipeline.");
			pipeline.execute();
			
			log.fine("Pipeline executing, waiting for completion.");
			pipeline.waitForCompletion();
			
			log.fine("Pipeline complete.");
			
		} catch (Throwable t) {
			log.log(Level.SEVERE, "Main thread aborted.", t);
		}
	}
	
	
	private static final void initializeLogging() {
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
		
		// Set the required logging level.
		rootLogger.setLevel(Level.ALL);
	}
}
