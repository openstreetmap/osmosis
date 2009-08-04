// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.apidb.v0_6.impl;

import java.text.DecimalFormat;
import java.text.NumberFormat;


/**
 * Formats replication sequence numbers into file names.
 */
public class ReplicationFileSequenceFormatter {
	private static final String SEQUENCE_FORMAT = "000000000";
	
	private NumberFormat sequenceFormat;
	
	
	/**
	 * Creates a new instance.
	 */
	public ReplicationFileSequenceFormatter() {
		sequenceFormat = new DecimalFormat(SEQUENCE_FORMAT);
	}
	
	
	/**
	 * Formats the sequence number.
	 * 
	 * @param sequenceNumber
	 *            The sequence number.
	 * @return The formatted replication file name.
	 */
	public String getFormattedName(long sequenceNumber) {
		return sequenceFormat.format(sequenceNumber);
	}
}
