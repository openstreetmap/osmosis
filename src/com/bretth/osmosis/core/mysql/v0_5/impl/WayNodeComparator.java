package com.bretth.osmosis.core.mysql.v0_5.impl;

import java.util.Comparator;


/**
 * Compares way segments to allow them to be sorted by way id then sequence
 * number.
 * 
 * @author Brett Henderson
 */
public class WayNodeComparator implements Comparator<WayNode> {
	
	/**
	 * {@inheritDoc}
	 */
	public int compare(WayNode o1, WayNode o2) {
		long way1Id;
		long way2Id;
		
		way1Id = o1.getWayId();
		way2Id = o2.getWayId();
		if (way1Id != way2Id) {
			if (way1Id < way2Id) {
				return -1;
			} else {
				return 1;
			}
		}
		
		return o1.getSequenceId() - o2.getSequenceId();
	}
}
