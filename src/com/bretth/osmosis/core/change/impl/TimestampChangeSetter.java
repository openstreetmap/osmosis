package com.bretth.osmosis.core.change.impl;

import java.util.Calendar;
import java.util.Date;

import com.bretth.osmosis.core.container.ChangeContainer;
import com.bretth.osmosis.core.container.EntityProcessor;
import com.bretth.osmosis.core.container.NodeContainer;
import com.bretth.osmosis.core.container.SegmentContainer;
import com.bretth.osmosis.core.container.WayContainer;
import com.bretth.osmosis.core.data.Node;
import com.bretth.osmosis.core.data.Segment;
import com.bretth.osmosis.core.data.Way;
import com.bretth.osmosis.core.task.ChangeAction;
import com.bretth.osmosis.core.task.ChangeSink;


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
		
		outputEntity = new Way(inputEntity.getId(), timestamp);
		
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
