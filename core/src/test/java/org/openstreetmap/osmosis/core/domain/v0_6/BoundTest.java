// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.domain.v0_6;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;


/**
 * Tests the Bound entity class.
 * 
 * @author Karl Newman
 */
public class BoundTest {

	/**
	 * Test the constructor with right > 180.
	 */
	@Test(expected = IllegalArgumentException.class)
	public final void testConstructor1() {
		new Bound(181.0000000000001, -20, 20, -20, "not null");
		fail("Expected to throw an exception");
	}


	/**
	 * Test the constructor with right < -180.
	 */
	@Test(expected = IllegalArgumentException.class)
	public final void testConstructor2() {
		new Bound(-181.0000000000001, -20, 20, -20, "not null");
		fail("Expected to throw an exception");
	}


	/**
	 * Test the constructor with left > 180.
	 */
	@Test(expected = IllegalArgumentException.class)
	public final void testConstructor3() {
		new Bound(20, 181.0000000000001, 20, -20, "not null");
		fail("Expected to throw an exception");
	}


	/**
	 * Test the constructor with left < -180.
	 */
	@Test(expected = IllegalArgumentException.class)
	public final void testConstructor4() {
		new Bound(20, -181.0000000000001, 20, -20, "not null");
		fail("Expected to throw an exception");
	}


	/**
	 * Test the constructor with top > 90.
	 */
	@Test(expected = IllegalArgumentException.class)
	public final void testConstructor5() {
		new Bound(20, -20, 91.0000000000001, -20, "not null");
		fail("Expected to throw an exception");
	}


	/**
	 * Test the constructor with top < -90.
	 */
	@Test(expected = IllegalArgumentException.class)
	public final void testConstructor6() {
		new Bound(20, -20, -91.0000000000001, -20, "not null");
		fail("Expected to throw an exception");
	}


	/**
	 * Test the constructor with bottom > 90.
	 */
	@Test(expected = IllegalArgumentException.class)
	public final void testConstructor7() {
		new Bound(20, -20, 20, 91.0000000000001, "not null");
		fail("Expected to throw an exception");
	}


	/**
	 * Test the constructor with bottom < -90.
	 */
	@Test(expected = IllegalArgumentException.class)
	public final void testConstructor8() {
		new Bound(20, -20, 20, -91.0000000000001, "not null");
		fail("Expected to throw an exception");
	}


	/**
	 * Test the constructor with top < bottom.
	 */
	@Test(expected = IllegalArgumentException.class)
	public final void testConstructor9() {
		new Bound(20, -20, -20, 20, "not null");
		fail("Expected to throw an exception");
	}


	/**
	 * Test a valid constructor with only the origin string provided (covers full planet).
	 */
	@Test
	public final void testConstructor11() {
		Bound b = new Bound("not null");
		assertTrue(Double.compare(b.getRight(), 180) == 0
		        && Double.compare(b.getLeft(), -180) == 0
		        && Double.compare(b.getTop(), 90) == 0
		        && Double.compare(b.getBottom(), -90) == 0
		        && b.getOrigin().equals("not null"));
	}


	/**
	 * Test a valid constructor with all values provided.
	 */
	@Test
	public final void testConstructor12() {
		Bound b = new Bound(20, -20, 21, -21, "not null");
		assertTrue(Double.compare(b.getRight(), 20) == 0
		        && Double.compare(b.getLeft(), -20) == 0
		        && Double.compare(b.getTop(), 21) == 0
		        && Double.compare(b.getBottom(), -21) == 0
		        && b.getOrigin().equals("not null"));
	}


	/**
	 * Test a simple intersection.
	 */
	@Test
	public final void testIntersect1() {
		final double right1 = 20.0;
		final double left1 = 10.0;
		final double top1 = 40.0;
		final double bottom1 = 30.0;
		final double right2 = 30.0;
		final double left2 = 15.0;
		final double top2 = 45.0;
		final double bottom2 = 20.0;

		Bound b1 = new Bound(right1, left1, top1, bottom1, "this");
		Bound b2 = new Bound(right2, left2, top2, bottom2, "that");
		Bound b = b1.intersect(b2);
		assertTrue(Double.compare(b.getRight(), right1) == 0
		        && Double.compare(b.getLeft(), left2) == 0
		        && Double.compare(b.getTop(), top1) == 0
		        && Double.compare(b.getBottom(), bottom1) == 0
		        && b.getOrigin().equals("this"));
		// Test it with arguments swapped
		b = b2.intersect(b1);
		assertTrue(Double.compare(b.getRight(), right1) == 0
		        && Double.compare(b.getLeft(), left2) == 0
		        && Double.compare(b.getTop(), top1) == 0
		        && Double.compare(b.getBottom(), bottom1) == 0
		        && b.getOrigin().equals("that"));
	}


	/**
	 * Test an intersect with no top-bottom overlapping areas.
	 */
	@Test
	public final void testIntersect2() {
		final double right1 = 20.0;
		final double left1 = 10.0;
		final double top1 = 40.0;
		final double bottom1 = 30.0;
		final double right2 = 30.0;
		final double left2 = 15.0;
		final double top2 = 25.0;
		final double bottom2 = 20.0;
		Bound b = new Bound(right1, left1, top1, bottom1, "").intersect(new Bound(
		        right2,
		        left2,
		        top2,
		        bottom2,
		        ""));
		assertNull(b);
	}


	/**
	 * Test an intersect with no left-right overlapping areas.
	 */
	@Test
	public final void testIntersect3() {
		final double right1 = 20.0;
		final double left1 = 10.0;
		final double top1 = 40.0;
		final double bottom1 = 30.0;
		final double right2 = 30.0;
		final double left2 = 21.0;
		final double top2 = 45.0;
		final double bottom2 = 20.0;
		Bound b = new Bound(right1, left1, top1, bottom1, "").intersect(new Bound(
		        right2,
		        left2,
		        top2,
		        bottom2,
		        ""));
		assertNull(b);
	}


	/**
	 * Test an intersect with 1 Bound crossing the antimeridian.
	 */
	@Test
	public final void testIntersect4() {
		final double right1 = 20.0;
		final double left1 = 60.0;
		final double top1 = 40.0;
		final double bottom1 = 30.0;
		final double right2 = 30.0;
		final double left2 = 15.0;
		final double top2 = 45.0;
		final double bottom2 = 20.0;

		Bound b1 = new Bound(right1, left1, top1, bottom1, "");
		Bound b2 = new Bound(right2, left2, top2, bottom2, "that");
		Bound b = b1.intersect(b2);
		assertTrue(Double.compare(b.getRight(), right1) == 0
		        && Double.compare(b.getLeft(), left2) == 0
		        && Double.compare(b.getTop(), top1) == 0
		        && Double.compare(b.getBottom(), bottom1) == 0
		        && b.getOrigin().equals("that"));
		// Test it with arguments swapped
		b = b2.intersect(b1);
		assertTrue(Double.compare(b.getRight(), right1) == 0
		        && Double.compare(b.getLeft(), left2) == 0
		        && Double.compare(b.getTop(), top1) == 0
		        && Double.compare(b.getBottom(), bottom1) == 0
		        && b.getOrigin().equals("that"));
	}


	/**
	 * Test an intersection where one Bound crosses the antimeridian (but doesn't cover the planet)
	 * and both ends overlap with the intersecting Bound. A strict intersection would result in two
	 * Bound areas, so just expect the smaller (longitudinally) of the Bound as the result.
	 */
	@Test
	public final void testIntersect5() {
		final double right1 = 150.0;
		final double left1 = 170.0;
		final double top1 = 40.0;
		final double bottom1 = 30.0;
		final double right2 = 175.0;
		final double left2 = 145.0;
		final double top2 = 45.0;
		final double bottom2 = 25.0;

		Bound b1 = new Bound(right1, left1, top1, bottom1, "");
		Bound b2 = new Bound(right2, left2, top2, bottom2, "");
		Bound b = b1.intersect(b2);
		assertTrue(Double.compare(b.getRight(), right2) == 0
		        && Double.compare(b.getLeft(), left2) == 0
		        && Double.compare(b.getTop(), top1) == 0
		        && Double.compare(b.getBottom(), bottom1) == 0);
		// Test it with arguments swapped
		b = b2.intersect(b1);
		assertTrue(Double.compare(b.getRight(), right2) == 0
		        && Double.compare(b.getLeft(), left2) == 0
		        && Double.compare(b.getTop(), top1) == 0
		        && Double.compare(b.getBottom(), bottom1) == 0);
	}


	/**
	 * Test an intersect with both Bound crossing the antimeridian.
	 */
	@Test
	public final void testIntersect6() {
		final double right1 = 20.0;
		final double left1 = 60.0;
		final double top1 = 40.0;
		final double bottom1 = 30.0;
		final double right2 = 30.0;
		final double left2 = 50.0;
		final double top2 = 45.0;
		final double bottom2 = 35.0;
		Bound b = new Bound(right1, left1, top1, bottom1, "").intersect(new Bound(
		        right2,
		        left2,
		        top2,
		        bottom2,
		        ""));
		assertTrue(Double.compare(b.getRight(), right1) == 0
		        && Double.compare(b.getLeft(), left1) == 0
		        && Double.compare(b.getTop(), top1) == 0
		        && Double.compare(b.getBottom(), bottom2) == 0);
	}


	/**
	 * Test a simple union on opposite sides of the planet with exactly the same distance between
	 * them on both sides. The smallest resulting union could wrap around the planet either way, so
	 * expect a simple Bound which does not cross the antimeridian.
	 */
	@Test
	public final void testUnion1() {
		final double right1 = 90.0;
		final double left1 = 80.0;
		final double top1 = 40.0;
		final double bottom1 = 30.0;
		final double right2 = -90.0;
		final double left2 = -100.0;
		final double top2 = 45.0;
		final double bottom2 = 35.0;

		Bound b1 = new Bound(right1, left1, top1, bottom1, "this");
		Bound b2 = new Bound(right2, left2, top2, bottom2, "that");
		Bound b = b1.union(b2);
		assertTrue(Double.compare(b.getRight(), right1) == 0
		        && Double.compare(b.getLeft(), left2) == 0
		        && Double.compare(b.getTop(), top2) == 0
		        && Double.compare(b.getBottom(), bottom1) == 0
		        && b.getOrigin().equals("this"));
		// Test it with arguments swapped
		b = b2.union(b1);
		assertTrue(Double.compare(b.getRight(), right1) == 0
		        && Double.compare(b.getLeft(), left2) == 0
		        && Double.compare(b.getTop(), top2) == 0
		        && Double.compare(b.getBottom(), bottom1) == 0
		        && b.getOrigin().equals("that"));
	}


	/**
	 * Test a union where one Bound is entirely contained by another.
	 */
	@Test
	public final void testUnion2() {
		final double right1 = 20.0;
		final double left1 = 10.0;
		final double top1 = 40.0;
		final double bottom1 = 30.0;
		final double right2 = 15.0;
		final double left2 = 12.0;
		final double top2 = 35.0;
		final double bottom2 = 32.0;

		Bound b1 = new Bound(right1, left1, top1, bottom1, "");
		Bound b2 = new Bound(right2, left2, top2, bottom2, "that");
		Bound b = b1.union(b2);
		assertTrue(Double.compare(b.getRight(), right1) == 0
		        && Double.compare(b.getLeft(), left1) == 0
		        && Double.compare(b.getTop(), top1) == 0
		        && Double.compare(b.getBottom(), bottom1) == 0
		        && b.getOrigin().equals("that"));
		// Test it with arguments swapped
		b = b2.union(b1);
		assertTrue(Double.compare(b.getRight(), right1) == 0
		        && Double.compare(b.getLeft(), left1) == 0
		        && Double.compare(b.getTop(), top1) == 0
		        && Double.compare(b.getBottom(), bottom1) == 0
		        && b.getOrigin().equals("that"));
	}


	/**
	 * Test a union of two simple Bound where the resulting Bound crosses the antimeridian.
	 */
	@Test
	public final void testUnion3() {
		final double right1 = 91.0;
		final double left1 = 80.0;
		final double top1 = 40.0;
		final double bottom1 = 30.0;
		final double right2 = -90.0;
		final double left2 = -100.0;
		final double top2 = 45.0;
		final double bottom2 = 35.0;

		Bound b1 = new Bound(right1, left1, top1, bottom1, "");
		Bound b2 = new Bound(right2, left2, top2, bottom2, "");
		Bound b = b1.union(b2);
		assertTrue(Double.compare(b.getRight(), right2) == 0
		        && Double.compare(b.getLeft(), left1) == 0
		        && Double.compare(b.getTop(), top2) == 0
		        && Double.compare(b.getBottom(), bottom1) == 0);
		// Test it with arguments swapped
		b = b2.union(b1);
		assertTrue(Double.compare(b.getRight(), right2) == 0
		        && Double.compare(b.getLeft(), left1) == 0
		        && Double.compare(b.getTop(), top2) == 0
		        && Double.compare(b.getBottom(), bottom1) == 0);
	}


	/**
	 * Test a union where one Bound crosses the antimeridian but there is still a gap such that the
	 * union does not cover the planet.
	 */
	@Test
	public final void testUnion4() {
		final double right1 = 10.0;
		final double left1 = 20.0;
		final double top1 = 40.0;
		final double bottom1 = 30.0;
		final double right2 = 15.0;
		final double left2 = 12.0;
		final double top2 = 35.0;
		final double bottom2 = 32.0;

		Bound b1 = new Bound(right1, left1, top1, bottom1, "");
		Bound b2 = new Bound(right2, left2, top2, bottom2, "");
		Bound b = b1.union(b2);
		assertTrue(Double.compare(b.getRight(), right2) == 0
		        && Double.compare(b.getLeft(), left1) == 0
		        && Double.compare(b.getTop(), top1) == 0
		        && Double.compare(b.getBottom(), bottom1) == 0);
		// Test it with arguments swapped
		b = b2.union(b1);
		assertTrue(Double.compare(b.getRight(), right2) == 0
		        && Double.compare(b.getLeft(), left1) == 0
		        && Double.compare(b.getTop(), top1) == 0
		        && Double.compare(b.getBottom(), bottom1) == 0);
	}


	/**
	 * Test a union where both Bound cross the antimeridian but do not cover the planet.
	 */
	@Test
	public final void testUnion5() {
		final double right1 = -170.0;
		final double left1 = 175.0;
		final double top1 = 40.0;
		final double bottom1 = 30.0;
		final double right2 = -175.0;
		final double left2 = 170.0;
		final double top2 = 35.0;
		final double bottom2 = 25.0;

		Bound b1 = new Bound(right1, left1, top1, bottom1, "");
		Bound b2 = new Bound(right2, left2, top2, bottom2, "");
		Bound b = b1.union(b2);
		assertTrue(Double.compare(b.getRight(), right1) == 0
		        && Double.compare(b.getLeft(), left2) == 0
		        && Double.compare(b.getTop(), top1) == 0
		        && Double.compare(b.getBottom(), bottom2) == 0);
		// Test it with arguments swapped
		b = b2.union(b1);
		assertTrue(Double.compare(b.getRight(), right1) == 0
		        && Double.compare(b.getLeft(), left2) == 0
		        && Double.compare(b.getTop(), top1) == 0
		        && Double.compare(b.getBottom(), bottom2) == 0);
	}


	/**
	 * Test a union where one Bound covers the planet left-right.
	 */
	@Test
	public final void testUnion6() {
		final double right1 = 180.0;
		final double left1 = -180.0;
		final double top1 = 40.0;
		final double bottom1 = 30.0;
		final double right2 = 15.0;
		final double left2 = 12.0;
		final double top2 = 45.0;
		final double bottom2 = 32.0;

		Bound b1 = new Bound(right1, left1, top1, bottom1, "");
		Bound b2 = new Bound(right2, left2, top2, bottom2, "");
		Bound b = b1.union(b2);
		assertTrue(Double.compare(b.getRight(), right1) == 0
		        && Double.compare(b.getLeft(), left1) == 0
		        && Double.compare(b.getTop(), top2) == 0
		        && Double.compare(b.getBottom(), bottom1) == 0);
		// Test it with arguments swapped
		b = b2.union(b1);
		assertTrue(Double.compare(b.getRight(), right1) == 0
		        && Double.compare(b.getLeft(), left1) == 0
		        && Double.compare(b.getTop(), top2) == 0
		        && Double.compare(b.getBottom(), bottom1) == 0);
	}


	/**
	 * Test a union where the Bound overlap and the resulting union covers the planet left-right.
	 */
	@Test
	public final void testUnion7() {
		final double right1 = 150.0;
		final double left1 = 170.0;
		final double top1 = 40.0;
		final double bottom1 = 30.0;
		final double right2 = 175.0;
		final double left2 = 145.0;
		final double top2 = 45.0;
		final double bottom2 = 25.0;
		final double minLongitude = -180.0;
		final double maxLongitude = 180.0;

		Bound b1 = new Bound(right1, left1, top1, bottom1, "");
		Bound b2 = new Bound(right2, left2, top2, bottom2, "");
		Bound b = b1.union(b2);
		assertTrue(Double.compare(b.getRight(), maxLongitude) == 0
		        && Double.compare(b.getLeft(), minLongitude) == 0
		        && Double.compare(b.getTop(), top2) == 0
		        && Double.compare(b.getBottom(), bottom2) == 0);
		// Test it with arguments swapped
		b = b2.union(b1);
		assertTrue(Double.compare(b.getRight(), maxLongitude) == 0
		        && Double.compare(b.getLeft(), minLongitude) == 0
		        && Double.compare(b.getTop(), top2) == 0
		        && Double.compare(b.getBottom(), bottom2) == 0);
	}


	/**
	 * Test the case where the Bound is already "simple" (i.e., doesn't cross the antimeridian).
	 */
	@Test
	public final void testToSimpleBound1() {
		final double left = -179.0;
		final double right = 179.0;
		final double top = 1.0;
		final double bottom = -1.0;
		boolean expected1found = false;
		int cnt = 0;
		Bound expected1 = new Bound(right, left, top, bottom, "");

		for (Bound b : new Bound(right, left, top, bottom, "").toSimpleBound()) {
			cnt++;
			if (Double.compare(b.getRight(), expected1.getRight()) == 0
			        && Double.compare(b.getLeft(), expected1.getLeft()) == 0
			        && Double.compare(b.getTop(), expected1.getTop()) == 0
			        && Double.compare(b.getBottom(), expected1.getBottom()) == 0) {
				expected1found = true;
			}
		}
		assertTrue(cnt == 1);
		assertTrue(expected1found);
	}


	/**
	 * Test the case where the Bound is split into two simple Bound elements, one on either side
	 * of the antimeridian.
	 */
	@Test
	public final void testToSimpleBound2() {
		final double left = 179.0;
		final double right = -179.0;
		final double top = 1.0;
		final double bottom = -1.0;
		final double minLongitude = -180.0;
		final double maxLongitude = 180.0;
		boolean expected1found = false, expected2found = false;
		int cnt = 0;
		Bound expected1 = new Bound(maxLongitude, left, top, bottom, "");
		Bound expected2 = new Bound(right, minLongitude, top, bottom, "");

		for (Bound b : new Bound(right, left, top, bottom, "").toSimpleBound()) {
			cnt++;
			if (Double.compare(b.getRight(), expected1.getRight()) == 0
			        && Double.compare(b.getLeft(), expected1.getLeft()) == 0
			        && Double.compare(b.getTop(), expected1.getTop()) == 0
			        && Double.compare(b.getBottom(), expected1.getBottom()) == 0) {
				expected1found = true;
			}
			if (Double.compare(b.getRight(), expected2.getRight()) == 0
			        && Double.compare(b.getLeft(), expected2.getLeft()) == 0
			        && Double.compare(b.getTop(), expected2.getTop()) == 0
			        && Double.compare(b.getBottom(), expected2.getBottom()) == 0) {
				expected2found = true;
			}
		}

		assertTrue(cnt == 2);
		assertTrue(expected1found);
		assertTrue(expected2found);
	}
}
