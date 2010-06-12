// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.container.v0_6;

import org.openstreetmap.osmosis.core.domain.v0_6.Bound;


/**
 * A container factory for bound objects.
 */
public class BoundContainerFactory implements EntityContainerFactory<Bound> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public EntityContainer createContainer(Bound entity) {
		return new BoundContainer(entity);
	}
}
