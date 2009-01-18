// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package com.bretth.osmosis.core.change.v0_6.impl;

import java.util.Calendar;

import com.bretth.osmosis.core.container.v0_6.BoundContainer;
import com.bretth.osmosis.core.container.v0_6.ChangeContainer;
import com.bretth.osmosis.core.container.v0_6.EntityProcessor;
import com.bretth.osmosis.core.container.v0_6.NodeContainer;
import com.bretth.osmosis.core.container.v0_6.RelationContainer;
import com.bretth.osmosis.core.container.v0_6.WayContainer;
import com.bretth.osmosis.core.domain.common.SimpleTimestampContainer;
import com.bretth.osmosis.core.domain.common.TimestampContainer;
import com.bretth.osmosis.core.domain.v0_6.NodeBuilder;
import com.bretth.osmosis.core.domain.v0_6.RelationBuilder;
import com.bretth.osmosis.core.domain.v0_6.WayBuilder;
import com.bretth.osmosis.core.task.common.ChangeAction;
import com.bretth.osmosis.core.task.v0_6.ChangeSink;


/**
 * An entity processor that copies the input entity but applies the current
 * timestamp before sending it to the change sink. Note that the same time will
 * be applied to all entities and will be the time that the internal timestamp
 * was first derived.
 */
public class TimestampChangeSetter implements EntityProcessor {
	private ChangeAction action;
	private ChangeSink changeSink;
	private TimestampContainer timestamp;
	private NodeBuilder nodeBuilder;
	private WayBuilder wayBuilder;
	private RelationBuilder relationBuilder;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param changeSink
	 *            The sink to send all changes to.
	 * @param action
	 *            The action to apply to all entities.
	 */
	public TimestampChangeSetter(ChangeSink changeSink, ChangeAction action) {
		Calendar calendar;
		
		this.changeSink = changeSink;
		this.action = action;
		
		calendar = Calendar.getInstance();
		calendar.set(Calendar.MILLISECOND, 0);
		timestamp = new SimpleTimestampContainer(calendar.getTime());
		
		nodeBuilder = new NodeBuilder();
		wayBuilder = new WayBuilder();
		relationBuilder = new RelationBuilder();
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void process(BoundContainer boundContainer) {
		changeSink.process(new ChangeContainer(boundContainer, action));
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void process(NodeContainer nodeContainer) {
		nodeBuilder.initialize(nodeContainer.getEntity());
		nodeBuilder.setTimestamp(timestamp);
		
		changeSink.process(
			new ChangeContainer(
				new NodeContainer(
					nodeBuilder.buildEntity()
				),
				action
			)
		);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void process(WayContainer wayContainer) {
		wayBuilder.initialize(wayContainer.getEntity());
		wayBuilder.setTimestamp(timestamp);
		
		changeSink.process(
			new ChangeContainer(
				new WayContainer(
					wayBuilder.buildEntity()
				),
				action
			)
		);
	}


	/**
	 * {@inheritDoc}
	 */
	public void process(RelationContainer relationContainer) {
		relationBuilder.initialize(relationContainer.getEntity());
		relationBuilder.setTimestamp(timestamp);
		
		changeSink.process(
			new ChangeContainer(
				new RelationContainer(
					relationBuilder.buildEntity()
				),
				action
			)
		);
	}
}
