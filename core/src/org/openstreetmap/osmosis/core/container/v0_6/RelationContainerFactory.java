// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.container.v0_6;

import org.openstreetmap.osmosis.core.domain.v0_6.Relation;


/**
 * A container factory for relation objects.
 */
public class RelationContainerFactory implements EntityContainerFactory<Relation> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public EntityContainer createContainer(Relation entity) {
		return new RelationContainer(entity);
	}
}
