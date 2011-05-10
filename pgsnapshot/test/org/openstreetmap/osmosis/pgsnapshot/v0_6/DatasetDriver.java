// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.pgsnapshot.v0_6;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.TimeZone;

import org.openstreetmap.osmosis.core.OsmosisRuntimeException;
import org.openstreetmap.osmosis.core.container.v0_6.Dataset;
import org.openstreetmap.osmosis.core.container.v0_6.DatasetContext;
import org.openstreetmap.osmosis.core.container.v0_6.EntityManager;
import org.openstreetmap.osmosis.core.domain.v0_6.CommonEntityData;
import org.openstreetmap.osmosis.core.domain.v0_6.Node;
import org.openstreetmap.osmosis.core.domain.v0_6.OsmUser;
import org.openstreetmap.osmosis.core.domain.v0_6.Tag;
import org.openstreetmap.osmosis.core.task.v0_6.DatasetSink;


/**
 * Performs queries and modifications to exercise the pgsql dataset implementation.
 * 
 * @author Brett Henderson
 */
public class DatasetDriver implements DatasetSink {
	
	private Date buildDate(String utcString) {
		SimpleDateFormat dateFormat;
		
		dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
		
		try {
			return dateFormat.parse(utcString);
		} catch (ParseException e) {
			throw new OsmosisRuntimeException("The date string (" + utcString + ") could not be parsed.", e);
		}
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void process(Dataset dataset) {
		DatasetContext dsCtx = dataset.createReader();
		
		try {
			EntityManager<Node> nodeManager = dsCtx.getNodeManager();
			OsmUser user;
			Node node;
			
			// Create the user for edits to be performed under. This is an existing user with an
			// updated name.
			user = new OsmUser(10, "user10b");
			
			// Modify node 1 to add a new tag.
			node = nodeManager.getEntity(1).getWriteableInstance();
			node.setUser(user);
			node.getTags().add(new Tag("change", "new tag"));
			nodeManager.modifyEntity(node);
			
			// Delete node 6.
			nodeManager.removeEntity(6);
			
			// Add node 7 using the NONE user.
			node = new Node(new CommonEntityData(7, 16, buildDate("2008-01-02 18:19:20"), OsmUser.NONE, 93), -11, -12);
			node.getTags().addAll(
					Arrays.asList(new Tag[]{new Tag("created_by", "Me7"), new Tag("change", "new node")}));
			nodeManager.addEntity(node);
			
			dsCtx.complete();
			
		} finally {
			dsCtx.release();
		}
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public void release() {
		// Do nothing.
	}

}
