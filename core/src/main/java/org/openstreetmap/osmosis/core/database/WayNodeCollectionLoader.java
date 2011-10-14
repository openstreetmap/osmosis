// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.database;

import java.util.Collection;

import org.openstreetmap.osmosis.core.domain.v0_6.Way;
import org.openstreetmap.osmosis.core.domain.v0_6.WayNode;


/**
 * Loads way nodes from ways.
 */
public class WayNodeCollectionLoader implements FeatureCollectionLoader<Way, WayNode> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Collection<WayNode> getFeatureCollection(Way entity) {
		return entity.getWayNodes();
	}
}
