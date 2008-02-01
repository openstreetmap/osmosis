// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package com.bretth.osmosis.core.pgsql.common.v0_5.impl;

import java.io.File;

import com.bretth.osmosis.core.pgsql.common.CopyFileWriter;


/**
 * Writes user data to a file in a format suitable for populating a user table
 * in the database using a COPY statement.
 * 
 * @author Brett Henderson
 */
public class UserFileWriter {
	
	private static final String ANONYMOUS_USER_NAME = "OpenStreetMap";
	
	
	private CopyFileWriter userWriter;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param userFile
	 *            The file to write user data to.
	 */
	public UserFileWriter(File userFile) {
		userWriter = new CopyFileWriter(userFile);
	}
	
	
	/**
	 * Writes the specified record to the database.
	 * 
	 * @param id
	 *            The id of the user record.
	 * @param userName
	 *            The name of the user.
	 */
	public void writeRecord(long id, String userName) {
		// Id
		userWriter.writeField(id);
		// User name
		if (userName == null || userName.length() <= 0) {
			userWriter.writeField(ANONYMOUS_USER_NAME);
		} else {
			userWriter.writeField(userName);
		}
		// Email address
		userWriter.writeField("osmosis@bretth.com");
		
		userWriter.endRecord();
	}
	
	
	/**
	 * Flushes all changes to file.
	 */
	public void complete() {
		userWriter.complete();
	}
	
	
	/**
	 * Releases all resources.
	 */
	public void release() {
		userWriter.release();
	}
}
