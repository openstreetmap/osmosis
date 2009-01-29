// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package org.openstreetmap.osmosis.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.CharConversionException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import org.openstreetmap.osmosis.core.OsmosisConstants;
import org.openstreetmap.osmosis.core.database.DatabaseLoginCredentials;
import org.openstreetmap.osmosis.core.xml.common.ProductionDbCharset;
import org.openstreetmap.osmosis.util.impl.User;
import org.openstreetmap.osmosis.util.impl.UserTableReader;


/**
 * Very basic app for reading the user table and checking if all records are in
 * double encoded UTF8 format. This is only useful on the production database
 * where the data is incorrectly doubly encoded. On a proper installation, this
 * is unnecessary.
 * 
 * @author Brett Henderson
 */
public class TestUsersForUtf8 {
	
	/**
	 * The main entry point.
	 * 
	 * @param args
	 *            The program arguments.
	 * @throws Exception
	 *             if an error occurs.
	 */
	public static void main(String[] args) throws Exception {
		if (args.length == 0) {
			printUsage();
		} else {
			run(args);
		}
	}
	
	
	private static void printUsage() {
		System.out.println("Osmosis Test Users for UTF8  Version " + OsmosisConstants.VERSION);
		System.out.println("Usage: osmosis-test-users-for-utf8 <host> <database> <user> <password> <result file>");
	}
	
	
	private static void run(String[] args) throws Exception {
		int argIndex;
		DatabaseLoginCredentials credentials;
		File resultFile;
		UserTableReader userReader;
		BufferedWriter fileWriter;
		
		argIndex = 0;
		
		// Initialise database credentials.
		credentials = new DatabaseLoginCredentials(
				args[argIndex++], args[argIndex++], args[argIndex++], args[argIndex++], false, false);
		
		// Determine which file to write the results to.
		resultFile = new File(args[argIndex++]);
		
		// Open the result file.
		fileWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(resultFile), "UTF-8"));
		
		userReader = new UserTableReader(credentials);
		
		while (userReader.hasNext()) {
			User user;
			
			user = userReader.next();
			
			fileWriter.write(Long.toString(user.getId()));
			if (user.isDataPublic()) {
				fileWriter.write(" public");
			} else {
				fileWriter.write(" private");
			}
			
			if (testUTF8(user.getDisplayName())) {
				fileWriter.write(" PASS");
			} else {
				fileWriter.write(" FAIL");
			}
			
			fileWriter.newLine();
		}
		
		// Release the database resources.
		userReader.release();
		
		// Close the result file.
		fileWriter.close();
	}
	
	
	private static boolean testUTF8(String displayName) throws Exception {
		ByteArrayOutputStream outBuffer;
		ByteArrayInputStream inBuffer;
		BufferedWriter writer;
		BufferedReader reader;
		
		outBuffer = new ByteArrayOutputStream();
		writer = new BufferedWriter(new OutputStreamWriter(outBuffer, new ProductionDbCharset()));
		
		writer.write(displayName);
		writer.close();
		
		try {
			inBuffer = new ByteArrayInputStream(outBuffer.toByteArray());
			reader = new BufferedReader(new InputStreamReader(inBuffer, "UTF-8"));
			
			reader.readLine();
			
		} catch (CharConversionException e) {
			return false;
		}
		
		return true;
	}
}
