// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.apidb.v0_6.impl;

import org.openstreetmap.osmosis.core.task.v0_6.ChangeSink;


/**
 * Provides {@link Replicator} with end points for the change streams that it produces.
 */
public interface ReplicationDestination extends ChangeSink, ReplicationStatePersister {
	// This interface exists only to combine the functionality of its parents.
}
