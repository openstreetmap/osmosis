// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.xml.common;

import org.junit.Assert;
import org.junit.Test;

import javax.xml.parsers.SAXParser;

/**
 * Test the SaxParserFactory.
 *
 * @author Brett Henderson
 */
public class SaxParserFactoryTest {
    /**
     * Verify that a parser is successfully created.
     */
    @Test
    public void testCreateParser() {
        SAXParser parser = SaxParserFactory.createParser();
        Assert.assertNotNull("Parser was not created", parser);
    }
}
