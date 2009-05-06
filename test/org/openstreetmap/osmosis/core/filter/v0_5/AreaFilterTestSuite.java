// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.filter.v0_5;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;


/**
 * A test suite for all area filtering tests.
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
  AreaFilterTest.class,
  BoundingBoxFilterTest.class,
  PolygonFilterTest.class
})
public class AreaFilterTestSuite {
    // Empty class; placeholder for suite annotations
}
