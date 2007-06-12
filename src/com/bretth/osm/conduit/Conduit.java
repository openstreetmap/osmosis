package com.bretth.osm.conduit; 

import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.bretth.osm.conduit.change.ChangeApplierFactory;
import com.bretth.osm.conduit.change.ChangeDeriverFactory;
import com.bretth.osm.conduit.filter.BoundingBoxFilterFactory;
import com.bretth.osm.conduit.misc.NullChangeWriterFactory;
import com.bretth.osm.conduit.misc.NullWriterFactory;
import com.bretth.osm.conduit.mysql.MysqlReaderFactory;
import com.bretth.osm.conduit.mysql.MysqlWriterFactory;
import com.bretth.osm.conduit.pipeline.Pipeline;
import com.bretth.osm.conduit.pipeline.TaskManagerFactory;
import com.bretth.osm.conduit.sort.ElementSorterFactory;
import com.bretth.osm.conduit.sort.TypeThenIdComparator;
import com.bretth.osm.conduit.xml.XmlChangeReaderFactory;
import com.bretth.osm.conduit.xml.XmlChangeWriterFactory;
import com.bretth.osm.conduit.xml.XmlReaderFactory;
import com.bretth.osm.conduit.xml.XmlWriterFactory;


/**
 * The main entry point for the Conduit application.
 * 
 * @author Brett Henderson
 */
public class Conduit {
	private static final String VERSION = "0.1";
	
	private static final Logger log = Logger.getLogger(Conduit.class.getName());
	
	
	/**
	 * The entry point to the application.
	 * 
	 * @param args The command line arguments.
	 */
	public static void main(String[] args) {
		try {
			Pipeline pipeline;
			
			initializeLogging();
			
			log.info("Conduit Version " + VERSION);
			registerTasks();
			
			pipeline = new Pipeline();
			
			log.fine("Preparing pipeline.");
			pipeline.prepare(args);
			
			log.fine("Executing pipeline.");
			pipeline.run();
			
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
	
	
	/**
	 * Registers all task type factories available for use.
	 */
	private static void registerTasks() {
		ElementSorterFactory elementSorterFactory;
		
		// Configure factories that require additional information.
		elementSorterFactory = new ElementSorterFactory();
		elementSorterFactory.registerComparator("TypeThenId", new TypeThenIdComparator());
		
		// Register factories.
		TaskManagerFactory.register("read-mysql", new MysqlReaderFactory());
		TaskManagerFactory.register("write-mysql", new MysqlWriterFactory());
		TaskManagerFactory.register("read-xml", new XmlReaderFactory());
		TaskManagerFactory.register("write-xml", new XmlWriterFactory());
		TaskManagerFactory.register("bounding-box", new BoundingBoxFilterFactory());
		TaskManagerFactory.register("derive-change", new ChangeDeriverFactory());
		TaskManagerFactory.register("apply-change", new ChangeApplierFactory());
		TaskManagerFactory.register("read-xml-change", new XmlChangeReaderFactory());
		TaskManagerFactory.register("write-xml-change", new XmlChangeWriterFactory());
		TaskManagerFactory.register("write-null", new NullWriterFactory());
		TaskManagerFactory.register("write-null-change", new NullChangeWriterFactory());
		TaskManagerFactory.register("sort", elementSorterFactory);
	}
}
