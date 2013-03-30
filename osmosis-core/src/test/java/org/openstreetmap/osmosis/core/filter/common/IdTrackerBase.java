// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.filter.common;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;


/**
 * Tests an id tracker implementation.  Sub-classes provide the actual implementation to be tested.
 */
public abstract class IdTrackerBase {

	private static final int TEST_VAL_1 = -100;
	private static final int TEST_VAL_2 = 0;
	private static final int TEST_VAL_3 = 100;
	private static final int TEST_VAL_4 = 10000;
	private static final int TEST_VAL_5 = 10000;

	private IdTracker idt;

	
	/**
	 * Performs pre-test activities.
	 */
	@Before
	public final void setUp() {
		idt = getImplementation();
	}
	
	/**
	 * Performs post-test activities.
	 */
	@After
	public final void tearDown() {
		idt = null;
	}
	
	/**
	 * Gets an object which implements the interface under test.
	 * 
	 * @return The implementation of this interface to test.
	 */
	protected abstract IdTracker getImplementation();
	
	/**
	 * Simple test of a single value.
	 */
	@Test
	public final void testSet1() {
		idt.set(TEST_VAL_1);
		assertTrue(idt.get(TEST_VAL_1));
	}


	/**
	 * Test three values, added in order of increasing value.
	 */
	@Test
	public final void testSet2() {
		idt.set(TEST_VAL_1);
		idt.set(TEST_VAL_2);
		idt.set(TEST_VAL_3);
		idt.set(TEST_VAL_4);
		idt.set(TEST_VAL_5);
		assertTrue(idt.get(TEST_VAL_1));
		assertTrue(idt.get(TEST_VAL_2));
		assertTrue(idt.get(TEST_VAL_3));
		assertTrue(idt.get(TEST_VAL_4));
		assertTrue(idt.get(TEST_VAL_5));
	}


	/**
	 * Test three values, added in order of decreasing value.
	 */
	@Test
	public final void testSet3() {
		idt.set(TEST_VAL_5);
		idt.set(TEST_VAL_4);
		idt.set(TEST_VAL_3);
		idt.set(TEST_VAL_2);
		idt.set(TEST_VAL_1);
		assertTrue(idt.get(TEST_VAL_1));
		assertTrue(idt.get(TEST_VAL_2));
		assertTrue(idt.get(TEST_VAL_3));
		assertTrue(idt.get(TEST_VAL_4));
		assertTrue(idt.get(TEST_VAL_5));
	}


	/**
	 * Test three values, added in random (not sorted) order.
	 */
	@Test
	public final void testSet4() {
		idt.set(TEST_VAL_2);
		idt.set(TEST_VAL_5);
		idt.set(TEST_VAL_3);
		idt.set(TEST_VAL_4);
		idt.set(TEST_VAL_1);
		assertTrue(idt.get(TEST_VAL_1));
		assertTrue(idt.get(TEST_VAL_2));
		assertTrue(idt.get(TEST_VAL_3));
		assertTrue(idt.get(TEST_VAL_4));
		assertTrue(idt.get(TEST_VAL_5));
	}


	/**
	 * Test duplicate values added.
	 */
	@Test
	public final void testSet5() {
		idt.set(TEST_VAL_1);
		idt.set(TEST_VAL_2);
		idt.set(TEST_VAL_3);
		idt.set(TEST_VAL_4);
		idt.set(TEST_VAL_5);
		idt.set(TEST_VAL_1);
		assertTrue(idt.get(TEST_VAL_1));
		assertTrue(idt.get(TEST_VAL_2));
		assertTrue(idt.get(TEST_VAL_3));
		assertTrue(idt.get(TEST_VAL_4));
		assertTrue(idt.get(TEST_VAL_5));
	}


	/**
	 * Test set after get.
	 */
	@Test
	public final void testSet6() {
		idt.set(TEST_VAL_2);
		assertTrue(idt.get(TEST_VAL_2));
		idt.set(TEST_VAL_1);
		idt.set(TEST_VAL_3);
		idt.set(TEST_VAL_4);
		idt.set(TEST_VAL_5);
		assertTrue(idt.get(TEST_VAL_1));
		assertTrue(idt.get(TEST_VAL_2));
		assertTrue(idt.get(TEST_VAL_3));
		assertTrue(idt.get(TEST_VAL_4));
		assertTrue(idt.get(TEST_VAL_5));
	}


	/**
	 * Test a large number of values added to trigger a growth in the list size.
	 */
	@Test
	public final void testSet7() {
		final int listSize = 1024;
		for (int i = listSize - 1; i >= 0; i--) {
			idt.set(i);
		}
		// This one should trigger the list growth
		idt.set(TEST_VAL_3);
		assertTrue(idt.get(TEST_VAL_3));
		for (int i = 0; i < listSize; i++) {
			assertTrue(idt.get(i));
		}
	}


	/**
	 * Tests the setAll method of the id tracker.
	 */
	@Ignore
	@Test
	public final void testSetAll() {
		fail("Not yet implemented"); // TODO
	}

}
