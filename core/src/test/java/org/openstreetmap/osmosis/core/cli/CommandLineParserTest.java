// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.cli;

import java.util.Arrays;
import java.util.logging.Level;

import org.junit.Assert;
import org.junit.Test;

import org.openstreetmap.osmosis.core.LogLevels;
import org.openstreetmap.osmosis.core.OsmosisRuntimeException;


/**
 * Tests the CommandLineParser class.
 * 
 * @author Brett Henderson
 */
public class CommandLineParserTest {
	/**
	 * Validates the quiet option.
	 */
	@Test
	public void testQuietOption() {
		CommandLineParser commandLineParser;
		
		commandLineParser = new CommandLineParser();
		commandLineParser.parse(new String [] {});
		Assert.assertEquals("Incorrect default log level.", Level.INFO,
				LogLevels.getLogLevel(commandLineParser.getLogLevelIndex()));
		
		commandLineParser = new CommandLineParser();
		commandLineParser.parse(new String [] {"-q"});
		Assert.assertEquals("Incorrect quiet log level.", Level.WARNING,
				LogLevels.getLogLevel(commandLineParser.getLogLevelIndex()));
		
		commandLineParser = new CommandLineParser();
		commandLineParser.parse(new String [] {"-q", "1"});
		Assert.assertEquals("Incorrect very quiet log level.", Level.SEVERE,
				LogLevels.getLogLevel(commandLineParser.getLogLevelIndex()));
		
		commandLineParser = new CommandLineParser();
		commandLineParser.parse(new String [] {"-q", "2"});
		Assert.assertEquals("Incorrect very very quiet log level.", Level.OFF,
				LogLevels.getLogLevel(commandLineParser.getLogLevelIndex()));
	}
	
	
	/**
	 * Validates the verbose option.
	 */
	@Test
	public void testVerboseOption() {
		CommandLineParser commandLineParser;
		
		commandLineParser = new CommandLineParser();
		commandLineParser.parse(new String [] {});
		Assert.assertEquals("Incorrect default log level.", Level.INFO,
				LogLevels.getLogLevel(commandLineParser.getLogLevelIndex()));
		
		commandLineParser = new CommandLineParser();
		commandLineParser.parse(new String [] {"-v"});
		Assert.assertEquals("Incorrect verbose log level.", Level.FINE,
				LogLevels.getLogLevel(commandLineParser.getLogLevelIndex()));
		
		commandLineParser = new CommandLineParser();
		commandLineParser.parse(new String [] {"-v", "1"});
		Assert.assertEquals("Incorrect very verbose log level.", Level.FINER,
				LogLevels.getLogLevel(commandLineParser.getLogLevelIndex()));
		
		commandLineParser = new CommandLineParser();
		commandLineParser.parse(new String [] {"-v", "2"});
		Assert.assertEquals("Incorrect very very verbose log level.", Level.FINEST,
				LogLevels.getLogLevel(commandLineParser.getLogLevelIndex()));
		
		commandLineParser = new CommandLineParser();
		commandLineParser.parse(new String [] {"-v", "3"});
		Assert.assertEquals(
				"Incorrect very very very verbose log level.",
				Level.FINEST,
				LogLevels.getLogLevel(commandLineParser.getLogLevelIndex()));
	}
	
	
	/**
	 * Validates the quiet and verbose options in combination.
	 */
	@Test
	public void testQuietAndVerboseOption() {
		CommandLineParser commandLineParser;
		
		commandLineParser = new CommandLineParser();
		commandLineParser.parse(new String [] {});
		Assert.assertEquals("Incorrect default log level.", Level.INFO,
				LogLevels.getLogLevel(commandLineParser.getLogLevelIndex()));
		
		commandLineParser = new CommandLineParser();
		commandLineParser.parse(new String [] {"-v", "-q"});
		Assert.assertEquals("Incorrect default log level.", Level.INFO,
				LogLevels.getLogLevel(commandLineParser.getLogLevelIndex()));
		
		commandLineParser = new CommandLineParser();
		commandLineParser.parse(new String [] {"-v", "1", "-q", "1"});
		Assert.assertEquals("Incorrect default log level.", Level.INFO,
				LogLevels.getLogLevel(commandLineParser.getLogLevelIndex()));
		
		commandLineParser = new CommandLineParser();
		commandLineParser.parse(new String [] {"-v", "1", "-q", "2"});
		Assert.assertEquals("Incorrect quiet log level.", Level.WARNING,
				LogLevels.getLogLevel(commandLineParser.getLogLevelIndex()));
	}
	
	
	/**
	 * Validates the quiet and verbose options in combination.
	 */
	@Test
	public void testPluginOption() {
		CommandLineParser commandLineParser;
		
		commandLineParser = new CommandLineParser();
		commandLineParser.parse(new String [] {"-p", "plugin1", "-p", "plugin2"});
		Assert.assertEquals(
				"Incorrect plugin list.",
				Arrays.asList("plugin1", "plugin2"),
				commandLineParser.getPlugins());
	}
	
	
	/**
	 * Validates failure when an unknown option is specified.
	 */
	@Test (expected = OsmosisRuntimeException.class)
	public void testUnknownOption() {
		CommandLineParser commandLineParser;
		
		commandLineParser = new CommandLineParser();
		commandLineParser.parse(new String [] {"-a"});
	}
}
