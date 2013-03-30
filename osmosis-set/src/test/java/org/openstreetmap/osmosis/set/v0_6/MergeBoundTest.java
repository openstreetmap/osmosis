// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.set.v0_6;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.osmosis.core.container.v0_6.BoundContainer;
import org.openstreetmap.osmosis.core.container.v0_6.EntityContainer;
import org.openstreetmap.osmosis.core.container.v0_6.NodeContainer;
import org.openstreetmap.osmosis.core.domain.v0_6.Bound;
import org.openstreetmap.osmosis.core.domain.v0_6.CommonEntityData;
import org.openstreetmap.osmosis.core.domain.v0_6.EntityType;
import org.openstreetmap.osmosis.core.domain.v0_6.Node;
import org.openstreetmap.osmosis.core.domain.v0_6.OsmUser;
import org.openstreetmap.osmosis.core.merge.common.ConflictResolutionMethod;
import org.openstreetmap.osmosis.core.misc.v0_6.EmptyReader;
import org.openstreetmap.osmosis.core.task.v0_6.RunnableSource;
import org.openstreetmap.osmosis.core.task.v0_6.Sink;
import org.openstreetmap.osmosis.testutil.v0_6.RunTaskUtilities;
import org.openstreetmap.osmosis.testutil.v0_6.SinkEntityInspector;


/**
 * Tests bounding box processing in merge tasks.
 * 
 * @author Igor Podolskiy
 */
public class MergeBoundTest {

	/**
	 * A simple dummy ID generator for the helper source class.
	 */
	private static AtomicInteger idGenerator = new AtomicInteger(1000);
	
	/**
	 * Tests the proper working of the merge task if neither 
	 * source has a declared bound.
	 * 
	 * @throws Exception if something goes wrong
	 */
	@Test
	public void testNeitherHasBound() throws Exception {
		RunnableSource source0 = new BoundSource(new Bound(1, 2, 4, 3, "source0"), false);
		RunnableSource source1 = new BoundSource(new Bound(5, 6, 8, 7, "source1"), false);

		EntityMerger merger = new EntityMerger(ConflictResolutionMethod.LatestSource, 1,
				BoundRemovedAction.Ignore);
		
		SinkEntityInspector merged = RunTaskUtilities.run(merger, source0, source1);
		List<EntityContainer> mergedList = createList(merged.getProcessedEntities());

		Assert.assertEquals(2, mergedList.size());
		for (EntityContainer entityContainer : mergedList) {
			Assert.assertEquals(EntityType.Node, entityContainer.getEntity().getType());
		}
	}
	
	/**
	 * Tests whether merge will delete the declared bound if only source 0 
	 * has a declared bound.
	 * 
	 * @throws Exception if something goes wrong
	 */
	@Test
	public void testSource0HasBound() throws Exception {
		RunnableSource source0 = new BoundSource(new Bound(1, 2, 4, 3, "source0"), true);
		RunnableSource source1 = new BoundSource(new Bound(5, 6, 8, 7, "source1"), false);

		EntityMerger merger = new EntityMerger(ConflictResolutionMethod.LatestSource, 1,
				BoundRemovedAction.Ignore);
		
		SinkEntityInspector merged = RunTaskUtilities.run(merger, source0, source1);

		List<EntityContainer> mergedList = createList(merged.getProcessedEntities());
		Assert.assertEquals(2, mergedList.size());
		for (EntityContainer entityContainer : mergedList) {
			Assert.assertEquals(EntityType.Node, entityContainer.getEntity().getType());
		}
	}
	
	/**
	 * Tests whether merge will delete the declared bound if only source 1 
	 * has a declared bound.
	 * 
	 * @throws Exception if something goes wrong
	 */
	@Test
	public void testSource1HasBound() throws Exception {
		RunnableSource source0 = new BoundSource(new Bound(1, 2, 4, 3, "source0"), false);
		RunnableSource source1 = new BoundSource(new Bound(5, 6, 8, 7, "source1"), true);

		EntityMerger merger = new EntityMerger(ConflictResolutionMethod.LatestSource, 1,
				BoundRemovedAction.Ignore);
		
		SinkEntityInspector merged = RunTaskUtilities.run(merger, source0, source1);
		List<EntityContainer> mergedList = createList(merged.getProcessedEntities());
		
		Assert.assertEquals(2, mergedList.size());
		for (EntityContainer entityContainer : mergedList) {
			Assert.assertEquals(EntityType.Node, entityContainer.getEntity().getType());
		}
	}
	
	/**
	 * Test the proper computation of the union bound iff both sources
	 * have bounds.
	 * 
	 * @throws Exception if something goes wrong
	 */
	@Test
	public void testBothHaveBounds() throws Exception {
		Bound bound0 = new Bound(1, 2, 4, 3, "source1");
		RunnableSource source0 = new BoundSource(bound0, true);

		Bound bound1 = new Bound(5, 6, 8, 7, "source2");
		RunnableSource source1 = new BoundSource(bound1, true);

		EntityMerger merger = new EntityMerger(ConflictResolutionMethod.LatestSource, 1,
				BoundRemovedAction.Ignore);
		
		SinkEntityInspector merged = RunTaskUtilities.run(merger, source0, source1);
		List<EntityContainer> mergedList = createList(merged.getProcessedEntities());
		Assert.assertEquals(3, mergedList.size());
		Assert.assertEquals(EntityType.Bound, mergedList.get(0).getEntity().getType());
		
		// Check the bound
		Bound bound01 = (Bound) mergedList.get(0).getEntity();
		Assert.assertEquals(bound0.union(bound1), bound01);

		for (int i = 1; i < mergedList.size(); i++) {
			Assert.assertEquals(EntityType.Node, mergedList.get(i).getEntity().getType());
		}
	}
	
	/**
	 * Tests the proper working of the merge task if both sources are
	 * empty.
	 * 
	 * @throws Exception if something goes wrong
	 */
	@Test
	public void testBothEmpty() throws Exception {
		RunnableSource source0 = new EmptyReader();
		RunnableSource source1 = new EmptyReader();

		EntityMerger merger = new EntityMerger(ConflictResolutionMethod.LatestSource, 1,
				BoundRemovedAction.Ignore);
		
		SinkEntityInspector merged = RunTaskUtilities.run(merger, source0, source1);
		Assert.assertTrue("Expected empty result set but got some data", merged.getLastEntityContainer() == null);
	}
	
	/**
	 * Tests the proper working of the merge task if exactly one source is
	 * empty with respect to the declared bound.
	 * 
	 * @throws Exception if something goes wrong
	 */
	@Test
	public void testOneSourceEmpty() throws Exception {
		RunnableSource source0 = new EmptyReader();

		Bound bound1 = new Bound(5, 6, 8, 7, "source2");
		RunnableSource source1 = new BoundSource(bound1, true);
		
		EntityMerger merger = new EntityMerger(ConflictResolutionMethod.LatestSource, 1,
				BoundRemovedAction.Ignore);
		
		SinkEntityInspector merged = RunTaskUtilities.run(merger, source0, source1);
		List<EntityContainer> mergedList = createList(merged.getProcessedEntities());
		
		Assert.assertEquals(2, mergedList.size());
		Assert.assertEquals(bound1, mergedList.get(0).getEntity());
		Assert.assertEquals(EntityType.Node, mergedList.get(1).getEntity().getType());
	}

	private static <T> List<T> createList(Iterable<T> t) {
		List<T> list = new ArrayList<T>();
		for (T elem : t) {
			list.add(elem);
		}
		return list;
	}

	/**
	 * A simple source which provides a single node in the center of a given
	 * bounding box, and optionally, a declared bounding box.
	 */
	private static class BoundSource implements RunnableSource {

		private Sink sink;
		private Bound bound;
		private boolean publishBound;

		public BoundSource(Bound bound, boolean publishBound) {
			if (bound == null) {
				throw new IllegalArgumentException("bound must not be null");
			}
			this.publishBound = publishBound;
			this.bound = bound;
		}

		@Override
		public void setSink(Sink sink) {
			this.sink = sink;
		}


		@Override
		public void run() {
			try {
				sink.initialize(Collections.<String, Object>emptyMap());
				if (publishBound) {
					sink.process(new BoundContainer(bound));
				}
				sink.process(new NodeContainer(createNode()));
				sink.complete();
			} finally {
				sink.release();
			}
		}
		
		private Node createNode() {
			double lon = (bound.getRight() - bound.getLeft()) / 2;
			double lat = (bound.getTop() - bound.getBottom()) / 2;
			return new Node(
					new CommonEntityData(idGenerator.incrementAndGet(), 1, new Date(), OsmUser.NONE, 1),
					lat, lon);
		}
	}
}
