// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.container.v0_6;

import org.openstreetmap.osmosis.core.domain.v0_6.Node;


/**
 * A container factory for node objects.
 */
public class NodeContainerFactory implements EntityContainerFactory<Node> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public EntityContainer createContainer(Node entity) {
		return new NodeContainer(entity);
	}
}
