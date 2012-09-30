// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.tagtransform;

import org.openstreetmap.osmosis.core.domain.v0_6.EntityType;


public enum TTEntityType {

	NODE, WAY, RELATION, BOUND;

	public EntityType getEntityType0_6() {
		switch (this) {
		case NODE:
			return EntityType.Node;
		case WAY:
			return EntityType.Way;
		case RELATION:
			return EntityType.Relation;
		case BOUND:
			return EntityType.Bound;
		default:
			return null;
		}
	}


	public static TTEntityType fromEntityType0_6(EntityType entityType) {
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
