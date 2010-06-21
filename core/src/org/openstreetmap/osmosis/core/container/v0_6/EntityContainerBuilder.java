// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.container.v0_6;

import org.openstreetmap.osmosis.core.domain.v0_6.EntityBuilder;
import org.openstreetmap.osmosis.core.domain.v0_6.NodeBuilder;
import org.openstreetmap.osmosis.core.domain.v0_6.RelationBuilder;
import org.openstreetmap.osmosis.core.domain.v0_6.WayBuilder;
import org.openstreetmap.osmosis.core.task.v0_6.Sink;
import org.openstreetmap.osmosis.core.task.v0_6.Source;


/**
 * Provides a mechanism to manipulate entities without directly manipulating and instantiating their
 * containers. This class does nothing by default, sub-classes must override methods to add their
 * own functionality
 * 
 * @author Brett Henderson
 * 
 * @deprecated The builder classes are not required because entities are now writeable.
 */
@Deprecated
public class EntityContainerBuilder implements EntityProcessor, Source {

	private Sink sink;
	private NodeBuilder nodeBuilder;
	private WayBuilder wayBuilder;
	private RelationBuilder relationBuilder;

	/**
	 * Creates a new instance.
	 */
	public EntityContainerBuilder() {
		nodeBuilder = new NodeBuilder();
		wayBuilder = new WayBuilder();
		relationBuilder = new RelationBuilder();
	}

	/**
	 * Performs generic entity processing. This should be overridden by
	 * implementations wishing to perform generic entity processing.
	 * 
	 * @param builder
	 *            The entity builder to be processed.
	 * @return True if modifications were made to the entity builder during
	 *         processing.
	 */
	protected boolean processEntity(EntityBuilder<?> builder) {
		return false;
	}

	/**
	 * Performs node specific processing. This should be overridden by
	 * implementations wishing to perform node processing.
	 * 
	 * @param entityBuilder
	 *            The entity builder to be processed.
	 * @return True if modifications were made to the entity builder during
	 *         processing.
	 */
	protected boolean processNode(NodeBuilder entityBuilder) {
		return false;
	}

	/**
	 * Performs way specific processing. This should be overridden by
	 * implementations wishing to perform way processing.
	 * 
	 * @param entityBuilder
	 *            The entity builder to be processed.
	 * @return True if modifications were made to the entity builder during
	 *         processing.
	 */
	protected boolean processWay(WayBuilder entityBuilder) {
		return false;
	}

	/**
	 * Performs relation specific processing. This should be overridden by
	 * implementations wishing to perform relation processing.
	 * 
	 * @param entityBuilder
	 *            The entity builder to be processed.
	 * @return True if modifications were made to the entity builder during
	 *         processing.
	 */
	protected boolean processRelation(RelationBuilder entityBuilder) {
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void process(BoundContainer boundContainer) {
		sink.process(boundContainer);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void process(NodeContainer nodeContainer) {
		boolean modified;
		
		nodeBuilder.initialize(nodeContainer.getEntity());
		
		modified = false;
		modified = modified || processEntity(nodeBuilder);
		modified = modified || processNode(nodeBuilder);
		
		if (modified) {
			sink.process(new NodeContainer(nodeBuilder.buildEntity()));
		} else {
			sink.process(nodeContainer);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void process(WayContainer wayContainer) {
		boolean modified;
		
		wayBuilder.initialize(wayContainer.getEntity());
		
		modified = false;
		modified = modified || processEntity(wayBuilder);
		modified = modified || processWay(wayBuilder);
		
		if (modified) {
			sink.process(new WayContainer(wayBuilder.buildEntity()));
		} else {
			sink.process(wayContainer);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void process(RelationContainer relationContainer) {
		boolean modified;
		
		relationBuilder.initialize(relationContainer.getEntity());
		
		modified = false;
		modified = modified || processEntity(relationBuilder);
		modified = modified || processRelation(relationBuilder);
		
		if (modified) {
			sink.process(new RelationContainer(relationBuilder.buildEntity()));
		} else {
			sink.process(relationContainer);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setSink(Sink sink) {
		this.sink = sink;
	}
}
