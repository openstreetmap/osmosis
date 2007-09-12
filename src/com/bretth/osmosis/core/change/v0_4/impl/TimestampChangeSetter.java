package com.bretth.osmosis.core.change.v0_4.impl;

import java.util.Calendar;
import java.util.Date;

import com.bretth.osmosis.core.container.v0_4.ChangeContainer;
import com.bretth.osmosis.core.container.v0_4.EntityProcessor;
import com.bretth.osmosis.core.container.v0_4.NodeContainer;
import com.bretth.osmosis.core.container.v0_4.SegmentContainer;
import com.bretth.osmosis.core.container.v0_4.WayContainer;
import com.bretth.osmosis.core.domain.v0_4.Node;
import com.bretth.osmosis.core.domain.v0_4.Segment;
import com.bretth.osmosis.core.domain.v0_4.Way;
import com.bretth.osmosis.core.task.common.ChangeAction;
import com.bretth.osmosis.core.task.v0_4.ChangeSink;


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
	public void process(NodeContainer nodeContainer) {
		Node inputEntity = nodeContainer.getEntity();
		
		changeSink.process(
			new ChangeContainer(
				new NodeContainer(
					new Node(
						inputEntity.getId(),
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
	public void process(SegmentContainer segmentContainer) {
		Segment inputEntity = segmentContainer.getEntity();
		
		changeSink.process(
			new ChangeContainer(
				new SegmentContainer(
					new Segment(
						inputEntity.getId(),
						timestamp,
						inputEntity.getUser(),
						inputEntity.getFrom(),
						inputEntity.getTo()
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
		
		outputEntity = new Way(inputEntity.getId(), timestamp, inputEntity.getUser());
		
		outputEntity.addSegmentReferences(inputEntity.getSegmentReferenceList());
		outputEntity.addTags(inputEntity.getTagList());
		
		changeSink.process(
			new ChangeContainer(
				new WayContainer(outputEntity),
				action
			)
		);
	}

}
