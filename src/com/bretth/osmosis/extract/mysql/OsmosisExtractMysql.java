package com.bretth.osmosis.extract.mysql;

import java.io.File;


/**
 * The main entry point for the mysql change extraction application.
 * 
 * @author Brett Henderson
 */
public class OsmosisExtractMysql {
	
	/**
	 * The entry point to the application.
	 * 
	 * @param args
	 *            The command line arguments.
	 */
	public static void main(String[] args) {
		File baseDirectory;
		Configuration configuration;
		
		// Base directory.
		baseDirectory = new File("C:/Documents and Settings/Administrator/My Documents/work/osmosis/replication_data");
		
		configuration = new Configuration(baseDirectory);
		
		configuration.getHost();
	}
}
