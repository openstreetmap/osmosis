package com.bretth.osmosis.mysql;

import java.util.Date;

import com.bretth.osmosis.OsmosisRuntimeException;
import com.bretth.osmosis.container.ChangeContainer;
import com.bretth.osmosis.container.WayContainer;
import com.bretth.osmosis.data.Way;
import com.bretth.osmosis.mysql.impl.EntityHistory;
import com.bretth.osmosis.mysql.impl.NodeChangeReader;
import com.bretth.osmosis.mysql.impl.SegmentChangeReader;
import com.bretth.osmosis.mysql.impl.WayHistoryReader;
import com.bretth.osmosis.mysql.impl.WaySegment;
import com.bretth.osmosis.mysql.impl.WaySegmentHistoryReader;
import com.bretth.osmosis.mysql.impl.WayTag;
import com.bretth.osmosis.mysql.impl.WayTagHistoryReader;
import com.bretth.osmosis.task.ChangeAction;
import com.bretth.osmosis.task.ChangeSink;
import com.bretth.osmosis.task.RunnableChangeSource;


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
	 * @param intervalBegin
	 *            Marks the beginning (inclusive) of the time interval to be
	 *            checked.
	 * @param intervalEnd
	 *            Marks the end (exclusive) of the time interval to be checked.
	 */
	public MysqlChangeReader(String host, String database, String user, String password, Date intervalBegin, Date intervalEnd) {
		this.host = host;
		this.database = database;
		this.user = user;
		this.password = password;
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
		NodeChangeReader reader = new NodeChangeReader(host, database, user, password, intervalBegin, intervalEnd);
		
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
		SegmentChangeReader reader = new SegmentChangeReader(host, database, user, password, intervalBegin, intervalEnd);
		
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
		WayHistoryReader wayHistoryReader;
		WaySegmentHistoryReader waySegmentHistoryReader;
		WayTagHistoryReader wayTagHistoryReader;
		
		wayHistoryReader = new WayHistoryReader(host, database, user, password, intervalBegin, intervalEnd);
		waySegmentHistoryReader = new WaySegmentHistoryReader(host, database, user, password, intervalBegin, intervalEnd);
		wayTagHistoryReader = new WayTagHistoryReader(host, database, user, password, intervalBegin, intervalEnd);
		
		try {
			while (wayHistoryReader.hasNext()) {
				EntityHistory<Way> wayHistory;
				Way way;
				
				wayHistory = wayHistoryReader.next();
				way = wayHistory.getEntity();
				
				while (
						waySegmentHistoryReader.hasNext() &&
						(waySegmentHistoryReader.peekNext().getEntity().getWayId() <= way.getId())) {
					
					EntityHistory<WaySegment> waySegmentHistory;
					WaySegment waySegment;
					
					waySegmentHistory = waySegmentHistoryReader.next();
					waySegment = waySegmentHistory.getEntity();
					
					if (waySegment.getWayId() == way.getId()) {
						way.addSegmentReference(waySegment);
					}
				}
				
				
				while (
						wayTagHistoryReader.hasNext() &&
						(wayTagHistoryReader.peekNext().getEntity().getWayId() <= way.getId())) {
					
					EntityHistory<WayTag> wayTagHistory;
					WayTag wayTag;
					
					wayTagHistory = wayTagHistoryReader.next();
					wayTag = wayTagHistory.getEntity();
					
					if (wayTag.getWayId() == way.getId()) {
						way.addTag(wayTag);
					}
				}
				
				// Ensure the version of the way is valid.
				if (wayHistory.getVersion() <= 0) {
					throw new OsmosisRuntimeException(
						"Way with id " + way.getId()
						+ " has a history element with version "
						+ wayHistory.getVersion()
						+ ".");
				}
				
				// If the version of the way is 1, it must be a create.
				// Else, if the change leaves it visible it is a modify.
				// Else, it is a delete.
				if (wayHistory.getVersion() == 1) {
					// By definition, a create must be visible but we'll double check to be sure.
					if (!wayHistory.isVisible()) {
						throw new OsmosisRuntimeException(
							"Way with id="
							+ way.getId()
							+ " is at version 1 but is not visible.");
					}
					
					changeSink.process(new ChangeContainer(new WayContainer(way), ChangeAction.Create));
					
				} else if (wayHistory.isVisible()) {
					changeSink.process(new ChangeContainer(new WayContainer(way), ChangeAction.Modify));
					
				} else {
					changeSink.process(new ChangeContainer(new WayContainer(way), ChangeAction.Delete));
				}
			}
			
		} finally {
			wayHistoryReader.release();
			waySegmentHistoryReader.release();
			wayTagHistoryReader.release();
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
