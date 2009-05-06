// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.filter.common;

import org.openstreetmap.osmosis.core.OsmosisRuntimeException;


/**
 * Creates IdTracker implementation instances depending on the requested id
 * tracker type.
 * 
 * @author Brett Henderson
 */
public class IdTrackerFactory {
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
		} else {
			throw new OsmosisRuntimeException("The IdTrackerType " + idTrackerType + " is not recognised.");
		}
	}
}
