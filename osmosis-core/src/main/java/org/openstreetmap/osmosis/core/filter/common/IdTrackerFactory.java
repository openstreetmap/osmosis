// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.filter.common;

import org.openstreetmap.osmosis.core.OsmosisRuntimeException;


/**
 * Creates IdTracker implementation instances depending on the requested id
 * tracker type.
 * 
 * @author Brett Henderson
 */
public final class IdTrackerFactory {
	
	/**
	 * This class cannot be instantiated.
	 */
	private IdTrackerFactory() {
	}
	
	
	/**
	 * Creates the requested id tracker type.
	 * 
	 * @param idTrackerType
	 *            The type of id tracker to instantiate.
	 * @return The new id tracker.
	 */
	public static IdTracker createInstance(IdTrackerType idTrackerType) {
		if (IdTrackerType.BitSet.equals(idTrackerType)) {
			return new BitSetIdTracker();
		} else if (IdTrackerType.IdList.equals(idTrackerType)) {
			return new ListIdTracker();
		} else if (IdTrackerType.Dynamic.equals(idTrackerType)) {
			return new DynamicIdTracker();
		} else {
			throw new OsmosisRuntimeException("The IdTrackerType " + idTrackerType + " is not recognised.");
		}
	}
}
