package com.bretth.osm.transformer; 

import java.io.File;

import com.bretth.osm.transformer.mysql.DatabaseReader;
import com.bretth.osm.transformer.mysql.DatabaseWriter;
import com.bretth.osm.transformer.xml.XmlReader;
import com.bretth.osm.transformer.xml.XmlWriter;


public class Main {
	
	public static void main(String[] args) {
		if (false) {
			doLoad();
		}
		if (true) {
			doDump();
		}
	}
	
	
	private static void doLoad() {
		File file;
		XmlReader loader;
		DatabaseWriter databaseWriter;
		
		//file = new File("/home/brett/work/osm/josm/Melbourne.osm");
		file = new File("/home/brett/tmp/planet-070321-utf8.osm");
		
		databaseWriter = new DatabaseWriter();
		loader = new XmlReader(databaseWriter);
		
		loader.process(file);
	}
	
	
	private static void doDump() {
		File file;
		XmlWriter xmlWriter;
		DatabaseReader databaseReader;
		
		file = new File("/home/brett/tmp/dbdump.osm");
		
		xmlWriter = new XmlWriter(file);
		databaseReader = new DatabaseReader(xmlWriter);
		
		databaseReader.process();
	}
}
