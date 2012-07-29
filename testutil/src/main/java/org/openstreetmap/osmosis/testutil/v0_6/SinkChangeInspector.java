// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.testutil.v0_6;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.openstreetmap.osmosis.core.container.v0_6.ChangeContainer;
import org.openstreetmap.osmosis.core.task.v0_6.ChangeSink;

/**
 * Mock object for inspecting the resulting changes after passing through a pipeline task.
 * 
 * @author Igor Podolskiy
 */
public class SinkChangeInspector implements ChangeSink {
	
	private List<ChangeContainer> receivedChanges;

	/**
	 * Creates a new instance.
	 */
	public SinkChangeInspector() {
		receivedChanges = new ArrayList<ChangeContainer>();
	}
	
	@Override
	public void initialize(Map<String, Object> metaData) {
		// Nothing to do here
	}

	@Override
	public void complete() {
		// Nothing to do here
	}

	@Override
	public void release() {
		// Nothing to do here
	}

	@Override
	public void process(ChangeContainer change) {
		receivedChanges.add(change);
	}
	
	/**
	 * Returns the list of the processed changes.
	 * 
	 * @return the list of the processed changes, never null.
	 */
	public List<ChangeContainer> getProcessedChanges() {
		return receivedChanges;
	}

	/**
	 * Returns the last processed change container, or null if no changes have
	 * been processed.
	 * 
	 * @return the last processed change container, or null if no changes have
	 *         been processed.
	 */
	public ChangeContainer getLastChangeContainer() {
		if (receivedChanges.isEmpty()) {
			return null;
		}
		return receivedChanges.get(receivedChanges.size() - 1);
	}

}
