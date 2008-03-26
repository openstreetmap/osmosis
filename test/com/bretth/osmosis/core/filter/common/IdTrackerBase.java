// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package com.bretth.osmosis.core.filter.common;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public abstract class IdTrackerBase {

	private static final int testVal1 = Integer.MIN_VALUE;
	private static final int testVal2 = 0;
	private static final int testVal3 = Integer.MAX_VALUE;

	protected IdTracker idt;

	@Before
	public final void setUp() {
		idt = getImplementation();
	}
	
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
		idt.set(testVal1);
		assertTrue(idt.get(testVal1));
	}


	/**
	 * Test three values, added in order of increasing value.
	 */
	@Test
	public final void testSet2() {
		idt.set(testVal1);
		idt.set(testVal2);
		idt.set(testVal3);
		assertTrue(idt.get(testVal1));
		assertTrue(idt.get(testVal2));
		assertTrue(idt.get(testVal3));
	}


	/**
	 * Test three values, added in order of decreasing value.
	 */
	@Test
	public final void testSet3() {
		idt.set(testVal3);
		idt.set(testVal2);
		idt.set(testVal1);
		assertTrue(idt.get(testVal1));
		assertTrue(idt.get(testVal2));
		assertTrue(idt.get(testVal3));
	}


	/**
	 * Test three values, added in random (not sorted) order.
	 */
	@Test
	public final void testSet4() {
		idt.set(testVal2);
		idt.set(testVal3);
		idt.set(testVal1);
		assertTrue(idt.get(testVal1));
		assertTrue(idt.get(testVal2));
		assertTrue(idt.get(testVal3));
	}


	/**
	 * Test duplicate values added.
	 */
	@Test
	public final void testSet5() {
		idt.set(testVal1);
		idt.set(testVal2);
		idt.set(testVal3);
		idt.set(testVal1);
		assertTrue(idt.get(testVal1));
		assertTrue(idt.get(testVal2));
		assertTrue(idt.get(testVal3));
	}


	/**
	 * Test set after get.
	 */
	@Test
	public final void testSet6() {
		idt.set(testVal2);
		assertTrue(idt.get(testVal2));
		idt.set(testVal1);
		idt.set(testVal3);
		assertTrue(idt.get(testVal1));
		assertTrue(idt.get(testVal2));
		assertTrue(idt.get(testVal3));
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
		idt.set(testVal3);
		assertTrue(idt.get(testVal3));
		for (int i = 0; i < listSize; i++) {
			assertTrue(idt.get(i));
		}
	}


	@Ignore
	@Test
	public final void testSetAll() {
		fail("Not yet implemented"); // TODO
	}

}
