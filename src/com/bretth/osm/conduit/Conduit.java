package com.bretth.osm.conduit; 

import java.io.File;

import com.bretth.osm.conduit.mysql.DatabaseReader;
import com.bretth.osm.conduit.mysql.DatabaseReaderManager;
import com.bretth.osm.conduit.mysql.DatabaseWriter;
import com.bretth.osm.conduit.mysql.DatabaseWriterManager;
import com.bretth.osm.conduit.xml.XmlReader;
import com.bretth.osm.conduit.xml.XmlWriter;


public class Conduit {
	
	public static void main(String[] args) {
		registerTasks();
		
		if (false) {
			doLoad();
		}
		if (true) {
			doDump();
		}
	}
	
	
	private static void registerTasks() {
		new DatabaseReaderManager();
		new DatabaseWriterManager();
	}
	
	
	private static void doLoad() {
		File file;
		XmlReader loader;
		DatabaseWriter databaseWriter;
		
		//file = new File("/home/brett/work/osm/josm/Melbourne.osm");
		file = new File("/home/brett/tmp/planet-070321-utf8.osm");
		
		databaseWriter = new DatabaseWriter();
		loader = new XmlReader(databaseWriter);
		loader.setFile(file);
		
		loader.run();
	}
	
	
	private static void doDump() {
		File file;
		XmlWriter xmlWriter;
		DatabaseReader databaseReader;
		
		file = new File("/home/brett/tmp/dbdump.osm");
		
		xmlWriter = new XmlWriter(file);
		databaseReader = new DatabaseReader(xmlWriter);
		
		databaseReader.run();
	}
}
