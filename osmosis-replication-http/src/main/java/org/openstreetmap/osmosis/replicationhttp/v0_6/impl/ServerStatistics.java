// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.replicationhttp.v0_6.impl;

/**
 * Captures statistics for a sequence server.
 * 
 * @author Brett Henderson
 */
public class ServerStatistics {
	private int totalRequests;
	private int activeConnections;


	/**
	 * Creates a new instance.
	 * 
	 * @param totalRequests
	 *            The total requests handled by the server.
	 * @param activeConnections
	 *            The current number of active connections.
	 */
	public ServerStatistics(int totalRequests, int activeConnections) {
		this.totalRequests = totalRequests;
		this.activeConnections = activeConnections;
	}


	/**
	 * Gets the total number of requests handled by the server.
	 * 
	 * @return The total number of requests.
	 */
	public int getTotalRequests() {
		return totalRequests;
	}


	/**
	 * Gets the current number of active connections.
	 * 
	 * @return The number of active connections.
	 */
	public int getActiveConnections() {
		return activeConnections;
	}
}
