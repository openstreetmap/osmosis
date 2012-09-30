// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.tagtransform.v0_6;

import org.openstreetmap.osmosis.core.container.v0_6.EntityContainer;
import org.openstreetmap.osmosis.core.task.v0_6.Sink;
import org.openstreetmap.osmosis.core.task.v0_6.SinkSource;
import org.openstreetmap.osmosis.tagtransform.impl.TransformHelper;


public class TransformTask extends TransformHelper<Sink> implements SinkSource {

	public TransformTask(String configFile, String statsFile) {
		super(configFile, statsFile);
	}


	@Override
	public void process(EntityContainer entityContainer) {
		EntityContainer output = processEntityContainer(entityContainer);
		if (output != null) {
			sink.process(output);
		}
	}

}
