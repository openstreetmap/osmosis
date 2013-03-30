// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.database;

import java.util.Collection;

import org.openstreetmap.osmosis.core.domain.v0_6.Relation;
import org.openstreetmap.osmosis.core.domain.v0_6.RelationMember;


/**
 * Loads relation members from relations.
 */
public class RelationMemberCollectionLoader implements FeatureCollectionLoader<Relation, RelationMember> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Collection<RelationMember> getFeatureCollection(Relation entity) {
		return entity.getMembers();
	}
}
