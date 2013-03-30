// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.sort.common;

import java.util.Comparator;
import java.util.Random;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.osmosis.core.lifecycle.ReleasableIterator;
import org.openstreetmap.osmosis.core.store.SingleClassObjectSerializationFactory;


/**
 * Tests the {@link FileBasedSort} class.
 */
public class FileBasedSortTest {

	/**
	 * Stores a large number of items into the file-based sorter and verifies
	 * that they are returned in the correct sequence. It exceeds the in-memory
	 * sorting limit, but doesn't trigger additional persistence at sub-levels
	 * in the merge sort to minimise file handles as this would take too much
	 * time for a unit test. The item count must be greater than
	 * (MAX_MEMORY_SORT_COUNT * (MAX_MERGE_SOURCE_COUNT ^
	 * MAX_MEMORY_SORT_DEPTH)) to trigger intermediate persistence.
	 */
	@Test
	public void test() {
		final long itemCount = 10000;

		// Create a new file-based sorter for the TestStoreable type.
		SingleClassObjectSerializationFactory objectFactory = new SingleClassObjectSerializationFactory(
				SampleStoreable.class);
		Comparator<SampleStoreable> comparator = new Comparator<SampleStoreable>() {
			@Override
			public int compare(SampleStoreable o1, SampleStoreable o2) {
				long value1 = o1.getValue();
				long value2 = o2.getValue();

				if (value1 > value2) {
					return 1;
				} else if (value1 < value2) {
					return -1;
				} else {
					return 0;
				}
			}
		};
		FileBasedSort<SampleStoreable> fileBasedSort =
				new FileBasedSort<SampleStoreable>(objectFactory, comparator, true);

		try {
			// Add randomly generated test values into the sorter.
			Random random = new Random();
			for (long i = 0; i < itemCount; i++) {
				fileBasedSort.add(new SampleStoreable(random.nextInt()));
			}

			// Read back all values in the sorter and verify that they are
			// sorted correctly.
			ReleasableIterator<SampleStoreable> resultIterator = fileBasedSort.iterate();
			try {
				int lastValue = Integer.MIN_VALUE;
				while (resultIterator.hasNext()) {
					int currentValue = resultIterator.next().getValue();
					Assert.assertTrue(currentValue >= lastValue);
					lastValue = currentValue;
				}
			} finally {
				resultIterator.release();
			}

		} finally {
			fileBasedSort.release();
		}
	}
}
