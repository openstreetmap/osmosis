// License: GPL. Copyright 2008 by Dave Stubbs and other contributors.
package uk.co.randomjunk.osmosis.transform.v0_6;

import org.openstreetmap.osmosis.core.container.v0_6.EntityContainer;
import org.openstreetmap.osmosis.core.task.v0_6.Sink;
import org.openstreetmap.osmosis.core.task.v0_6.SinkSource;

import uk.co.randomjunk.osmosis.transform.impl.TransformHelper;

public class TransformTask extends TransformHelper<Sink> implements SinkSource {

	public TransformTask(String configFile, String statsFile) {
		super(configFile, statsFile);
	}
	
	@Override
	public void process(EntityContainer entityContainer) {
		EntityContainer output = processEntityContainer(entityContainer);
		if ( output != null )
			sink.process(output);
	}


}
