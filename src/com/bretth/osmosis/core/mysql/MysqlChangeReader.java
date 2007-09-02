package com.bretth.osmosis.core.mysql;

import java.util.Date;

import com.bretth.osmosis.core.mysql.impl.NodeChangeReader;
import com.bretth.osmosis.core.mysql.impl.SegmentChangeReader;
import com.bretth.osmosis.core.mysql.impl.WayChangeReader;
import com.bretth.osmosis.core.task.ChangeSink;
import com.bretth.osmosis.core.task.RunnableChangeSource;


/**
 * A change source reading from database history tables. This aims to be
 * suitable for running at regular intervals with database overhead proportional
 * to changeset size.
 * 
 * @author Brett Henderson
 */
public class MysqlChangeReader implements RunnableChangeSource {
	private ChangeSink changeSink;
	private String host;
	private String database;
	private String user;
	private String password;
	private boolean readAllUsers;
	private Date intervalBegin;
	private Date intervalEnd;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param host
	 *            The server hosting the database.
	 * @param database
	 *            The database instance.
	 * @param user
	 *            The user name for authentication.
	 * @param password
	 *            The password for authentication.
	 * @param readAllUsers
	 *            If this flag is true, all users will be read from the database
	 *            regardless of their public edits flag.
	 * @param intervalBegin
	 *            Marks the beginning (inclusive) of the time interval to be
	 *            checked.
	 * @param intervalEnd
	 *            Marks the end (exclusive) of the time interval to be checked.
	 */
	public MysqlChangeReader(String host, String database, String user, String password, boolean readAllUsers, Date intervalBegin, Date intervalEnd) {
		this.host = host;
		this.database = database;
		this.user = user;
		this.password = password;
		this.readAllUsers = readAllUsers;
		this.intervalBegin = intervalBegin;
		this.intervalEnd = intervalEnd;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void setChangeSink(ChangeSink changeSink) {
		this.changeSink = changeSink;
	}
	
	
	/**
	 * Reads all node changes and sends them to the change sink.
	 */
	private void processNodes() {
		NodeChangeReader reader = new NodeChangeReader(host, database, user, password, readAllUsers, intervalBegin, intervalEnd);
		
		try {
			while (reader.hasNext()) {
				changeSink.process(reader.next());
			}
			
		} finally {
			reader.release();
		}
	}
	
	
	/**
	 * Reads all segment changes and sends them to the change sink.
	 */
	private void processSegments() {
		SegmentChangeReader reader = new SegmentChangeReader(host, database, user, password, readAllUsers, intervalBegin, intervalEnd);
		
		try {
			while (reader.hasNext()) {
				changeSink.process(reader.next());
			}
			
		} finally {
			reader.release();
		}
	}
	
	
	/**
	 * Reads all ways from the database and sends to the sink.
	 */
	private void processWays() {
		WayChangeReader reader = new WayChangeReader(host, database, user, password, readAllUsers, intervalBegin, intervalEnd);
		
		try {
			while (reader.hasNext()) {
				changeSink.process(reader.next());
			}
			
		} finally {
			reader.release();
		}
	}
	
	
	/**
	 * Reads all data from the file and send it to the sink.
	 */
	public void run() {
		try {
			processNodes();
			processSegments();
			processWays();
			
			changeSink.complete();
			
		} finally {
			changeSink.release();
		}
	}
}
