package com.bretth.osm.conduit; 

import com.bretth.osm.conduit.change.ChangeApplierFactory;
import com.bretth.osm.conduit.change.ChangeDeriverFactory;
import com.bretth.osm.conduit.filter.BoundingBoxFilterFactory;
import com.bretth.osm.conduit.misc.NullChangeWriterFactory;
import com.bretth.osm.conduit.misc.NullWriterFactory;
import com.bretth.osm.conduit.mysql.MysqlReaderFactory;
import com.bretth.osm.conduit.mysql.MysqlWriterFactory;
import com.bretth.osm.conduit.pipeline.Pipeline;
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
		new MysqlReaderFactory("read-mysql");
		new MysqlWriterFactory("write-mysql");
		new XmlReaderFactory("read-xml");
		new XmlWriterFactory("write-xml");
		new BoundingBoxFilterFactory("bounding-box");
		new ChangeDeriverFactory("derive-change");
		new ChangeApplierFactory("apply-change");
		new XmlChangeReaderFactory("read-xml-change");
		new XmlChangeWriterFactory("write-xml-change");
		new NullWriterFactory("write-null");
		new NullChangeWriterFactory("write-null-change");
	}
}
