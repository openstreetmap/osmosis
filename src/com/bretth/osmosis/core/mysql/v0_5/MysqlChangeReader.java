package com.bretth.osmosis.core.mysql.v0_5;

import java.util.Date;

import com.bretth.osmosis.core.mysql.common.DatabaseLoginCredentials;
import com.bretth.osmosis.core.mysql.v0_5.impl.NodeChangeReader;
import com.bretth.osmosis.core.mysql.v0_5.impl.WayChangeReader;
import com.bretth.osmosis.core.task.v0_5.ChangeSink;
import com.bretth.osmosis.core.task.v0_5.RunnableChangeSource;


/**
 * A change source reading from database history tables. This aims to be
 * suitable for running at regular intervals with database overhead proportional
 * to changeset size.
 * 
 * @author Brett Henderson
 */
public class MysqlChangeReader implements RunnableChangeSource {
	private ChangeSink changeSink;
	private DatabaseLoginCredentials loginCredentials;
	private boolean readAllUsers;
	private Date intervalBegin;
	private Date intervalEnd;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param loginCredentials
	 *            Contains all information required to connect to the database.
	 * @param readAllUsers
	 *            If this flag is true, all users will be read from the database
	 *            regardless of their public edits flag.
	 * @param intervalBegin
	 *            Marks the beginning (inclusive) of the time interval to be
	 *            checked.
	 * @param intervalEnd
	 *            Marks the end (exclusive) of the time interval to be checked.
	 */
	public MysqlChangeReader(DatabaseLoginCredentials loginCredentials, boolean readAllUsers, Date intervalBegin, Date intervalEnd) {
		this.loginCredentials = loginCredentials;
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
		NodeChangeReader reader = new NodeChangeReader(loginCredentials, readAllUsers, intervalBegin, intervalEnd);
		
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
		WayChangeReader reader = new WayChangeReader(loginCredentials, readAllUsers, intervalBegin, intervalEnd);
		
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
			processWays();
			
			changeSink.complete();
			
		} finally {
			changeSink.release();
		}
	}
}
