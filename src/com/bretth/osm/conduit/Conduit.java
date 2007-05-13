package com.bretth.osm.conduit; 

import com.bretth.osm.conduit.filter.BoundingBoxFilterFactory;
import com.bretth.osm.conduit.mysql.DatabaseReaderFactory;
import com.bretth.osm.conduit.mysql.DatabaseWriterFactory;
import com.bretth.osm.conduit.pipeline.Pipeline;
import com.bretth.osm.conduit.xml.XmlReaderFactory;
import com.bretth.osm.conduit.xml.XmlWriterFactory;


/**
 * The main entry point for the Conduit application.
 * 
 * @author Brett Henderson
 */
public class Conduit {
	
	/**
	 * The entry point to the application.
	 * 
	 * @param args The command line arguments.
	 */
	public static void main(String[] args) {
		registerTasks();
		
		Pipeline pipeline;
		
		pipeline = new Pipeline();
		
		pipeline.prepare(args);
		
		pipeline.run();
		
		pipeline.waitForCompletion();
	}
	
	
	/**
	 * Registers all task type factories available for use.
	 */
	private static void registerTasks() {
		new DatabaseReaderFactory();
		new DatabaseWriterFactory();
		new XmlReaderFactory();
		new XmlWriterFactory();
		new BoundingBoxFilterFactory();
	}
}
