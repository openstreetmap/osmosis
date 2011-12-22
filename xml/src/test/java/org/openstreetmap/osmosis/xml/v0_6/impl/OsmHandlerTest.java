// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.xml.v0_6.impl;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.junit.Before;
import org.junit.Test;
import org.xml.sax.SAXException;

import org.openstreetmap.osmosis.core.OsmosisRuntimeException;
import org.openstreetmap.osmosis.core.domain.v0_6.Bound;
import org.openstreetmap.osmosis.testutil.v0_6.SinkEntityInspector;


/**
 * Not sure how to go about unit testing this. The individual parser classes seem to require a lot
 * of infrastructure, so this test will just set up the full parser to parse an XML string and check
 * the produced entities.
 * 
 * @author Karl Newman
 * 
 */
public class OsmHandlerTest {

	private SAXParser parser;
	private SinkEntityInspector entityInspector;
	private static final String OSM_PREFIX = "<osm version=\"0.6\">\n";
	private static final String OSM_SUFFIX = "</osm>";


	/**
	 * Performs pre-test activities.
	 */
	@Before
	public void setUp() {
		entityInspector = new SinkEntityInspector();
		try {
			parser = SAXParserFactory.newInstance().newSAXParser();
		} catch (ParserConfigurationException e) {
			throw new OsmosisRuntimeException("Unable to create SAX Parser.", e);
		} catch (SAXException e) {
			throw new OsmosisRuntimeException("Unable to create SAX Parser.", e);
		}
	}


	private void parseString(String input) {
		InputStream inputStream = null;
		try {
			inputStream = new ByteArrayInputStream(input.getBytes("UTF-8"));
			parser.parse(inputStream, new OsmHandler(entityInspector, true));
		} catch (UnsupportedEncodingException e) {
			throw new OsmosisRuntimeException("String encoding exception", e);
		} catch (SAXException e) {
			throw new OsmosisRuntimeException("Parse exception", e);
		} catch (IOException e) {
			throw new OsmosisRuntimeException("IOException", e);
		} finally {
			try {
				if (inputStream != null) {
					inputStream.close();
				}
			} catch (IOException e) {
				throw new OsmosisRuntimeException("IOException", e);
			} finally {
				inputStream = null;
			}
		}
	}


	/**
	 * Tests that an empty xml document can be parsed successfully.
	 */
	@Test
	public final void testEmptyDocument() {
		parseString(OSM_PREFIX + OSM_SUFFIX);
		assertNull(entityInspector.getLastEntityContainer());
	}


	/**
	 * Test a normal, well-formed bound element.
	 */
	@Test
	public final void testBoundElement1() {
		parseString(OSM_PREFIX
		        + "<bound box=\"-12.34567,-23.45678,34.56789,45.67891\""
		        + " origin=\"someorigin\"/>"
		        + OSM_SUFFIX);
		Bound b = (Bound) entityInspector.getLastEntityContainer().getEntity();
		assertTrue(Double.compare(b.getRight(), 45.67891) == 0
		        && Double.compare(b.getLeft(), -23.45678) == 0
		        && Double.compare(b.getTop(), 34.56789) == 0
		        && Double.compare(b.getBottom(), -12.34567) == 0);
		assertTrue(b.getOrigin().equals("someorigin"));
	}


	/**
	 * Test a malformed box attribute for a bound element.
	 */
	@Test(expected = OsmosisRuntimeException.class)
	public final void testBoundElement2() {
		parseString(OSM_PREFIX
		        + "<bound box=\"-12.34567,-23.45678,34.56789\""
		        + " origin=\"someorigin\"/>"
		        + OSM_SUFFIX);
		fail("Expected to throw an exception");
	}


	/**
	 * Test a missing box attribute of a bound element.
	 */
	@Test(expected = OsmosisRuntimeException.class)
	public final void testBoundElement3() {
		parseString(OSM_PREFIX + "<bound origin=\"someorigin\"/>" + OSM_SUFFIX);
		fail("Expected to throw an exception");
	}


	/**
	 * Test a number parse error for a box attribute of a bound element.
	 */
	@Test(expected = OsmosisRuntimeException.class)
	public final void testBoundElement4() {
		parseString(OSM_PREFIX
		        + "<bound box=\"-12..34567,-23.45678,34.56789,45.67891\""
		        + " origin=\"someorigin\"/>"
		        + OSM_SUFFIX);
		fail("Expected to throw an exception");
	}


	/**
	 * Test a missing origin attribute of a bound element.
	 */
	@Test(expected = OsmosisRuntimeException.class)
	public final void testBoundElement5() {
		parseString(OSM_PREFIX
		        + "<bound box=\"-12.34567,-23.45678,34.56789,45.67891\"/>"
		        + OSM_SUFFIX);
		fail("Expected to throw an exception");
	}


	/**
	 * Test an empty origin attribute of a bound element.
	 */
	@Test(expected = OsmosisRuntimeException.class)
	public final void testBoundElement6() {
		parseString(OSM_PREFIX
		        + "<bound box=\"-12.34567,-23.45678,34.56789,45.67891\""
		        + " origin=\"\"/>"
		        + OSM_SUFFIX);
		fail("Expected to throw an exception");
	}


	/**
	 * Test a repeated bound element.
	 */
	@Test(expected = OsmosisRuntimeException.class)
	public final void testBoundElement7() {
		parseString(OSM_PREFIX
		        + "<bound box=\"-12.34567,-23.45678,34.56789,45.67891\""
		        + " origin=\"someorigin\"/>"
		        + "<bound box=\"-12.34567,-23.45678,34.56789,45.67891\""
		        + " origin=\"someotherorigin\"/>"
		        + OSM_SUFFIX);
		fail("Expected to throw an exception");
	}


	/**
	 * Test a bound element occurring after a node element.
	 */
	@Test(expected = OsmosisRuntimeException.class)
	public final void testBoundElement8() {
		parseString(OSM_PREFIX
		        + "<node id=\"12345\" user=\"OsmosisTest\" uid=\"12\" version=\"0\""
		        + "timestamp=\"2008-01-01T15:32:01\" lat=\"-12.34567\" lon=\"-23.45678\"/>"
		        + "<bound box=\"-12.34567,-23.45678,34.56789,45.67891\""
		        + " origin=\"someorigin\"/>"
		        + OSM_SUFFIX);
		fail("Expected to throw an exception");
	}


	/**
	 * Test a bound element occurring after a way element.
	 */
	@Test(expected = OsmosisRuntimeException.class)
	public final void testBoundElement9() {
		parseString(OSM_PREFIX
		        + "<way id=\"12346\" user=\"OsmosisTest\" uid=\"12\" version=\"0\""
		        + "timestamp=\"2008-01-01T15:32:01\">"
		        + "<nd ref=\"12345\"/>"
		        + "<nd ref=\"12347\"/>"
		        + "</way>"
		        + "<bound box=\"-12.34567,-23.45678,34.56789,45.67891\""
		        + " origin=\"someorigin\"/>"
		        + OSM_SUFFIX);
		fail("Expected to throw an exception");
	}


	/**
	 * Test a bound element occurring after a relation element.
	 */
	@Test(expected = OsmosisRuntimeException.class)
	public final void testBoundElement10() {
		parseString(OSM_PREFIX
		        + "<relation id=\"12348\" user=\"OsmosisTest\" uid=\"12\" version=\"0\""
		        + "timestamp=\"2008-01-01T15:32:01\">"
		        + "<member ref=\"12345\" type=\"node\" role=\"node1\"/>"
		        + "<member ref=\"12346\" type=\"way\" role=\"way1\"/>"
		        + "</relation>"
		        + "<bound box=\"-12.34567,-23.45678,34.56789,45.67891\""
		        + " origin=\"someorigin\"/>"
		        + OSM_SUFFIX);
		fail("Expected to throw an exception");
	}
	
	/**
	 * Test the parsing of a bound element as returned by the OSM API
	 * without an origin.
	 */
	@Test
	public void testBoundsNoOrigin() {
		parseString(OSM_PREFIX
				+ "<bounds minlat=\"-1.234\" minlon=\"-1.234\" maxlat=\"1.234\" maxlon=\"1.234\"/>"
				+ OSM_SUFFIX);
		Bound b = (Bound) entityInspector.getLastEntityContainer().getEntity();
		assertEquals(-1.234, b.getLeft(), 1E-6);
		assertEquals(-1.234, b.getBottom(), 1E-6);
		assertEquals(1.234, b.getRight(), 1E-6);
		assertEquals(1.234, b.getTop(), 1E-6);
		assertNull(b.getOrigin());
	}
	
	/**
	 * Test the parsing of a bound element as returned by the OSM API
	 * with an origin.
	 */
	@Test
	public void testBoundsWithOrigin() {
		parseString(OSM_PREFIX
				+ "<bounds minlat=\"-1\" minlon=\"-1\" maxlat=\"1\" maxlon=\"1\" " 
				+ " origin=\"someorigin\"/>"
				+ OSM_SUFFIX);
		Bound b = (Bound) entityInspector.getLastEntityContainer().getEntity();
		assertEquals("someorigin", b.getOrigin());
	}
	
	/**
	 * Test the inheritance of generator to the origin.
	 */
	@Test
	public void testBoundsOriginInheritance() {
		parseString("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
					+ "<osm version=\"0.6\" generator=\"somegenerator\">"
					+ "<bounds minlat=\"-1.234\" minlon=\"-1.234\" maxlat=\"1.234\" maxlon=\"1.234\"/>"
					+ "</osm>");
		Bound b = (Bound) entityInspector.getLastEntityContainer().getEntity();
		assertEquals("somegenerator", b.getOrigin());
	}
}
