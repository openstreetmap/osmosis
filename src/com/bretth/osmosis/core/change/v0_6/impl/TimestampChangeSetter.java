// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package com.bretth.osmosis.core.change.v0_6.impl;

import java.util.Calendar;
import java.util.Date;

import com.bretth.osmosis.core.container.v0_6.ChangeContainer;
import com.bretth.osmosis.core.container.v0_6.EntityProcessor;
import com.bretth.osmosis.core.container.v0_6.BoundContainer;
import com.bretth.osmosis.core.container.v0_6.NodeContainer;
import com.bretth.osmosis.core.container.v0_6.RelationContainer;
import com.bretth.osmosis.core.container.v0_6.WayContainer;
import com.bretth.osmosis.core.domain.v0_6.Node;
import com.bretth.osmosis.core.domain.v0_6.Relation;
import com.bretth.osmosis.core.domain.v0_6.Way;
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
	private Date timestamp;
	
	
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
		timestamp = calendar.getTime();
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
		Node inputEntity = nodeContainer.getEntity();
		
		changeSink.process(
			new ChangeContainer(
				new NodeContainer(
					new Node(
						inputEntity.getId(),
						inputEntity.getVersion(),
						timestamp,
						inputEntity.getUser(),
						inputEntity.getLatitude(),
						inputEntity.getLongitude()
					)
				),
				action
			)
		);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void process(WayContainer wayContainer) {
		Way inputEntity = wayContainer.getEntity();
		Way outputEntity;
		
		outputEntity = new Way(inputEntity.getId(),
							inputEntity.getVersion(),
							timestamp,
							inputEntity.getUser());
		
		outputEntity.addTags(inputEntity.getTagList());
		
		changeSink.process(
			new ChangeContainer(
				new WayContainer(outputEntity),
				action
			)
		);
	}


	/**
	 * {@inheritDoc}
	 */
	public void process(RelationContainer relationContainer) {
		Relation inputEntity = relationContainer.getEntity();
		Relation outputEntity;
		
		outputEntity = new Relation(inputEntity.getId(),
							inputEntity.getVersion(),
							timestamp,
							inputEntity.getUser());
		
		outputEntity.addTags(inputEntity.getTagList());
		
		changeSink.process(
			new ChangeContainer(
				new RelationContainer(outputEntity),
				action
			)
		);
	}
}
