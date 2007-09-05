package com.bretth.osmosis.core.mysql;

import com.bretth.osmosis.core.container.v0_4.NodeContainer;
import com.bretth.osmosis.core.container.v0_4.SegmentContainer;
import com.bretth.osmosis.core.container.v0_4.WayContainer;
import com.bretth.osmosis.core.domain.v0_4.Node;
import com.bretth.osmosis.core.domain.v0_4.Segment;
import com.bretth.osmosis.core.domain.v0_4.Way;
import com.bretth.osmosis.core.mysql.impl.CurrentNodeReader;
import com.bretth.osmosis.core.mysql.impl.CurrentSegmentReader;
import com.bretth.osmosis.core.mysql.impl.CurrentWayReader;
import com.bretth.osmosis.core.store.ReleasableIterator;
import com.bretth.osmosis.core.task.v0_4.RunnableSource;
import com.bretth.osmosis.core.task.v0_4.Sink;


/**
 * An OSM data source reading from a databases current tables. The entire
 * contents of the database are read.
 * 
 * @author Brett Henderson
 */
public class MySqlCurrentReader implements RunnableSource {
	private Sink sink;
	private String host;
	private String database;
	private String user;
	private String password;
	private boolean readAllUsers;
	
	
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
	 */
	public MySqlCurrentReader(String host, String database, String user, String password, boolean readAllUsers) {
		this.host = host;
		this.database = database;
		this.user = user;
		this.password = password;
		this.readAllUsers = readAllUsers;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void setSink(Sink sink) {
		this.sink = sink;
	}
	
	
	/**
	 * Reads all nodes from the database and sends to the sink.
	 */
	private void processNodes() {
		ReleasableIterator<Node> reader;
		
		reader = new CurrentNodeReader(host, database, user, password, readAllUsers);
		
		try {
			while (reader.hasNext()) {
				sink.process(new NodeContainer(reader.next()));
			}
			
		} finally {
			reader.release();
		}
	}
	
	
	/**
	 * Reads all segments from the database and sends to the sink.
	 */
	private void processSegments() {
		ReleasableIterator<Segment> reader;
		
		reader = new CurrentSegmentReader(host, database, user, password, readAllUsers);
		
		try {
			while (reader.hasNext()) {
				sink.process(new SegmentContainer(reader.next()));
			}
			
		} finally {
			reader.release();
		}
	}
	
	
	/**
	 * Reads all ways from the database and sends to the sink.
	 */
	private void processWays() {
		ReleasableIterator<Way> reader;
		
		reader = new CurrentWayReader(host, database, user, password, readAllUsers);
		
		try {
			while (reader.hasNext()) {
				sink.process(new WayContainer(reader.next()));
			}
			
		} finally {
			reader.release();
		}
	}
	
	
	/**
	 * Reads all data from the database and send it to the sink.
	 */
	public void run() {
		try {
			processNodes();
			processSegments();
			processWays();
			
			sink.complete();
			
		} finally {
			sink.release();
		}
	}
}
