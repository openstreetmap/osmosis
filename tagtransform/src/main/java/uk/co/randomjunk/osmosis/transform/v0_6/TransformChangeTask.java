// License: GPL. Copyright 2008 by Dave Stubbs and other contributors.
package uk.co.randomjunk.osmosis.transform.v0_6;

import org.openstreetmap.osmosis.core.container.v0_6.ChangeContainer;
import org.openstreetmap.osmosis.core.container.v0_6.EntityContainer;
import org.openstreetmap.osmosis.core.task.common.ChangeAction;
import org.openstreetmap.osmosis.core.task.v0_6.ChangeSink;
import org.openstreetmap.osmosis.core.task.v0_6.ChangeSinkChangeSource;

import uk.co.randomjunk.osmosis.transform.impl.TransformHelper;

public class TransformChangeTask extends TransformHelper<ChangeSink> implements ChangeSinkChangeSource {

	public TransformChangeTask(String configFile, String statsFile) {
		super(configFile, statsFile);
	}

	@Override
	public void process(ChangeContainer changeContainer) {
		if ( !ChangeAction.Delete.equals(changeContainer.getAction()) ) { 
			EntityContainer output = super.processEntityContainer(changeContainer.getEntityContainer());
			
			if ( output != null )
				sink.process( new ChangeContainer(output, changeContainer.getAction()) );
		} else {
			sink.process(changeContainer);
		}
	}

	@Override
	public void setChangeSink(ChangeSink changeSink) {
		this.setSink(changeSink);
	}

}
