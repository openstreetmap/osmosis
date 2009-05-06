// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.change.v0_6.impl;

import java.util.Calendar;

import org.openstreetmap.osmosis.core.container.v0_6.EntityContainer;
import org.openstreetmap.osmosis.core.domain.common.SimpleTimestampContainer;
import org.openstreetmap.osmosis.core.domain.common.TimestampContainer;


/**
 * Updates the current timestamp on to entities. Note that the same time will be applied to all
 * entities and will be the time that the internal timestamp was first derived.
 */
public class TimestampSetter {
	private TimestampContainer timestamp;
	
	
	/**
	 * Creates a new instance.
	 */
	public TimestampSetter() {
		Calendar calendar;
		
		calendar = Calendar.getInstance();
		calendar.set(Calendar.MILLISECOND, 0);
		timestamp = new SimpleTimestampContainer(calendar.getTime());
	}
	
	
	/**
	 * Updates the timestamp on the supplied entity. A new entity container may be created if the
	 * existing one is read-only.
	 * 
	 * @param entityContainer
	 *            The container holding the entity to be modified.
	 * @return A container holding an updated entity.
	 */
	public EntityContainer updateTimestamp(EntityContainer entityContainer) {
		EntityContainer resultContainer;
		
		resultContainer = entityContainer.getWriteableInstance();
		resultContainer.getEntity().setTimestampContainer(timestamp);
		
		return resultContainer;
	}
}
