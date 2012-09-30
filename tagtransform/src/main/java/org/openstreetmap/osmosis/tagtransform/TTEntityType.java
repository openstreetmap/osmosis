// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.tagtransform;

import org.openstreetmap.osmosis.core.domain.v0_6.EntityType;


public enum TTEntityType {

	NODE, WAY, RELATION, BOUND;


	public static TTEntityType fromEntityType06(EntityType entityType) {
		switch (entityType) {
		case Node:
			return NODE;
		case Way:
			return WAY;
		case Relation:
			return RELATION;
		case Bound:
			return BOUND;
		default:
			return null;
		}
	}
}
