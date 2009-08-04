// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.container.v0_6;

import org.openstreetmap.osmosis.core.domain.v0_6.Way;


/**
 * A container factory for way objects.
 */
public class WayContainerFactory implements EntityContainerFactory<Way> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public EntityContainer createContainer(Way entity) {
		return new WayContainer(entity);
	}
}
