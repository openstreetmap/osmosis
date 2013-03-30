// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.report.v0_6;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.openstreetmap.osmosis.core.OsmosisRuntimeException;
import org.openstreetmap.osmosis.core.container.v0_6.BoundContainer;
import org.openstreetmap.osmosis.core.container.v0_6.EntityContainer;
import org.openstreetmap.osmosis.core.container.v0_6.EntityProcessor;
import org.openstreetmap.osmosis.core.container.v0_6.NodeContainer;
import org.openstreetmap.osmosis.core.container.v0_6.RelationContainer;
import org.openstreetmap.osmosis.core.container.v0_6.WayContainer;
import org.openstreetmap.osmosis.core.task.v0_6.Sink;


/**
 * An OSM data sink that analyses the data sent to it and provides a simple
 * report.
 * 
 * @author Brett Henderson
 */
public class EntityReporter implements Sink {
	
	private static final int COLUMN_WIDTH_USER_NAME = 50;
	private static final int COLUMN_WIDTH_NODE_COUNT = 7;
	private static final int COLUMN_WIDTH_WAY_COUNT = 7;
	private static final int COLUMN_WIDTH_RELATION_COUNT = 7;
	
	private Logger log = Logger.getLogger(EntityReporter.class.getName());
	
	private File file;
	private FileWriter fileWriter;
	private Map<String, UserStatistics> userMap;
	private UserStatistics anonymousUser;
	private UserStatistics totalUser;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param file
	 *            The file to write.
	 */
	public EntityReporter(File file) {
		this.file = file;
		
		userMap = new HashMap<String, UserStatistics>();
		anonymousUser = new UserStatistics("anonymous");
		totalUser = new UserStatistics("Total");
	}


	/**
	 * {@inheritDoc}
	 */
	public void initialize(Map<String, Object> metaData) {
		// Do nothing.
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void process(EntityContainer entityContainer) {
		String userName;
		final UserStatistics user;
		
		// Obtain the user statistics object.
		userName = entityContainer.getEntity().getUser().getName();
		if (userName != null && userName.length() > 0) {
			if (userMap.containsKey(userName)) {
				user = userMap.get(userName);
			} else {
				user = new UserStatistics(userName);
				userMap.put(userName, user);
			}
		} else {
			user = anonymousUser;
		}
		
		// Increment the relevant user statistic.
		entityContainer.process(
			new EntityProcessor() {
				private UserStatistics processorUser = user;
				
				public void process(BoundContainer bound) {
					// Do nothing.
				}
				
				public void process(NodeContainer node) {
					processorUser.incrementNodeCount();
					totalUser.incrementNodeCount();
				}
				
				public void process(WayContainer way) {
					processorUser.incrementWayCount();
					totalUser.incrementWayCount();
				}
				
				public void process(RelationContainer relation) {
					processorUser.incrementRelationCount();
					totalUser.incrementRelationCount();
				}
			}
		);
	}
	
	
	/**
	 * Writes a single value and pads it out to the correct column width.
	 * 
	 * @param writer
	 *            The report destination.
	 * @param data
	 *            The data to be written.
	 * @param columnWidth
	 *            The width of the column.
	 */
	private void writeColumnValue(BufferedWriter writer, String data, int columnWidth) throws IOException {
		int padLength;
		
		// Calculate the required data padding. The total column width is the
		// specified column width plus one space.
		padLength = columnWidth - data.length() + 1;
		if (padLength < 1) {
			padLength = 1;
		}
		
		writer.write(data);
		for (int i = 0; i < padLength; i++) {
			writer.write(' ');
		}
	}
	
	
	/**
	 * Writes a single line summary of the user statistics.
	 * 
	 * @param writer
	 *            The report destination.
	 * @param userStatistics
	 *            The user to report on.
	 */
	private void writeUserLine(BufferedWriter writer, UserStatistics userStatistics) throws IOException {
		writeColumnValue(writer, userStatistics.getUserName(), COLUMN_WIDTH_USER_NAME);
		writeColumnValue(writer, Integer.toString(userStatistics.getNodeCount()), COLUMN_WIDTH_NODE_COUNT);
		writeColumnValue(writer, Integer.toString(userStatistics.getWayCount()), COLUMN_WIDTH_WAY_COUNT);
		writeColumnValue(writer, Integer.toString(userStatistics.getRelationCount()), COLUMN_WIDTH_RELATION_COUNT);
		writer.newLine();
	}
	
	
	/**
	 * Add user information to the report file.
	 * 
	 * @param writer
	 *            The report destination.
	 */
	private void writeUserReport(BufferedWriter writer) throws IOException {
		List<UserStatistics> userList;
		
		// Sort the user statistics by user id.
		userList = new ArrayList<UserStatistics>(userMap.values());
		Collections.sort(
			userList,
			new Comparator<UserStatistics>() {
				public int compare(UserStatistics o1, UserStatistics o2) {
					return o1.getUserName().compareTo(o2.getUserName());
				}
			}
		);
		
		writer.write("********** User Report **********");
		writer.newLine();
		writeColumnValue(writer, "USER NAME", COLUMN_WIDTH_USER_NAME);
		writeColumnValue(writer, "NODES", COLUMN_WIDTH_NODE_COUNT);
		writeColumnValue(writer, "WAYS", COLUMN_WIDTH_WAY_COUNT);
		writeColumnValue(writer, "RELNS", COLUMN_WIDTH_RELATION_COUNT);
		writer.newLine();
		writeUserLine(writer, anonymousUser);
		for (UserStatistics userStatistics : userList) {
			writeUserLine(writer, userStatistics);
		}
		writer.newLine();
		writeUserLine(writer, totalUser);
	}
	
	
	/**
	 * Flushes all changes to file.
	 */
	public void complete() {
		try {
			BufferedWriter writer;
			
			fileWriter = new FileWriter(file);
			writer = new BufferedWriter(fileWriter);
			
			// Produce a report on the user statistics.
			writeUserReport(writer);
			
			writer.close();
			fileWriter = null;
			
		} catch (IOException e) {
			throw new OsmosisRuntimeException("Unable to write report to file " + file + ".");
		}
	}
	
	
	/**
	 * Cleans up any open file handles.
	 */
	public void release() {
		if (fileWriter != null) {
			try {
				fileWriter.close();
			} catch (IOException e) {
				log.log(Level.SEVERE, "Unable to close file writer for file " + file + ".", e);
			} finally {
				fileWriter = null;
			}
		}
	}
	
	
	/**
	 * A class holding the summary information for a single user.
	 */
	private static class UserStatistics {
		
		private String userName;
		private int nodeCount;
		private int wayCount;
		private int relationCount;
		
		
		/**
		 * Creates a new instance.
		 * 
		 * @param userName
		 *            The name of the user that this statistics record relates
		 *            to.
		 */
		public UserStatistics(String userName) {
			this.userName = userName;
		}
		
		
		/**
		 * Increments the node count by one.
		 */
		public void incrementNodeCount() {
			nodeCount++;
		}
		
		
		/**
		 * Increments the way count by one.
		 */
		public void incrementWayCount() {
			wayCount++;
		}
		
		
		/**
		 * Increments the relation count by one.
		 */
		public void incrementRelationCount() {
			relationCount++;
		}
		
		
		/**
		 * Returns the name of the user for which this object contains data.
		 * 
		 * @return The user name.
		 */
		public String getUserName() {
			return userName;
		}
		
		
		/**
		 * Returns the node count.
		 * 
		 * @return The node count.
		 */
		public int getNodeCount() {
			return nodeCount;
		}
		
		
		/**
		 * Returns the way count.
		 * 
		 * @return The way count.
		 */
		public int getWayCount() {
			return wayCount;
		}
		
		
		/**
		 * Returns the relation count.
		 * 
		 * @return The relation count.
		 */
		public int getRelationCount() {
			return relationCount;
		}
	}
}
