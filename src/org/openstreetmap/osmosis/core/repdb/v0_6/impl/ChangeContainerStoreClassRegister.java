// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.repdb.v0_6.impl;

import org.openstreetmap.osmosis.core.container.v0_6.NodeContainer;
import org.openstreetmap.osmosis.core.container.v0_6.RelationContainer;
import org.openstreetmap.osmosis.core.container.v0_6.WayContainer;
import org.openstreetmap.osmosis.core.store.StaticStoreClassRegister;


/**
 * A store class register that knows about all classes contained within a change container.
 */
public class ChangeContainerStoreClassRegister extends StaticStoreClassRegister {

	/**
	 * Creates a new instance.
	 */
	public ChangeContainerStoreClassRegister() {
		super(new Class<?>[] {NodeContainer.class, WayContainer.class, RelationContainer.class});
	}
}
