// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package com.bretth.osmosis.core.xml.common;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderResult;

import com.bretth.osmosis.core.OsmosisRuntimeException;


/**
 * A special character set encoder to work around the production OSM database
 * issue of double encoded data.
 * 
 * @author Brett Henderson
 */
public class ProductionDbDataEncoder extends CharsetEncoder {
	
	/**
	 * Creates a new instance.
	 * 
	 * @param cs
	 *            The owning character set.
	 * @param averageBytesPerChar
	 *            The average number of output bytes per character.
	 * @param maxBytesPerChar
	 *            The maximum number of output bytes per character.
	 */
	protected ProductionDbDataEncoder(Charset cs, float averageBytesPerChar, float maxBytesPerChar) {
		super(cs, averageBytesPerChar, maxBytesPerChar);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected CoderResult encodeLoop(CharBuffer in, ByteBuffer out) {
		while (true) {
			char nextValue;
			
			if (!in.hasRemaining()) {
				return CoderResult.UNDERFLOW;
			}
			if (!out.hasRemaining()) {
				return CoderResult.OVERFLOW;
			}
			
			nextValue = in.get();
			
			if (nextValue >= 0x0000 && nextValue < 0x0080) {
				// No translation required for this range of characters.
				out.put((byte) nextValue);
			} else if (nextValue >= 0x00A0 && nextValue < 0x0100) {
				// No translation required for this range of characters.
				out.put((byte) nextValue);
			} else {
				switch (nextValue) {
				case 0x20AC:
					out.put((byte) 0x80);
					break;
				case 0x0081: // This is supposed to be an unmappable character.
					out.put((byte) 0x81);
					break;
				case 0x201A:
					out.put((byte) 0x82);
					break;
				case 0x0192:
					out.put((byte) 0x83);
					break;
				case 0x201E:
					out.put((byte) 0x84);
					break;
				case 0x2026:
					out.put((byte) 0x85);
					break;
				case 0x2020:
					out.put((byte) 0x86);
					break;
				case 0x2021:
					out.put((byte) 0x87);
					break;
				case 0x02C6:
					out.put((byte) 0x88);
					break;
				case 0x2030:
					out.put((byte) 0x89);
					break;
				case 0x0160:
					out.put((byte) 0x8A);
					break;
				case 0x2039:
					out.put((byte) 0x8B);
					break;
				case 0x0152:
					out.put((byte) 0x8C);
					break;
				case 0x008D: // This is supposed to be an unmappable character.
					out.put((byte) 0x8D);
					break;
				case 0x017D:
					out.put((byte) 0x8E);
					break;
				case 0x008F: // This is supposed to be an unmappable character.
					out.put((byte) 0x8F);
					break;
				case 0x0090: // This is supposed to be an unmappable character.
					out.put((byte) 0x90);
					break;
				case 0x2018:
					out.put((byte) 0x91);
					break;
				case 0x2019:
					out.put((byte) 0x92);
					break;
				case 0x201C:
					out.put((byte) 0x93);
					break;
				case 0x201D:
					out.put((byte) 0x94);
					break;
				case 0x2022:
					out.put((byte) 0x95);
					break;
				case 0x2013:
					out.put((byte) 0x96);
					break;
				case 0x2014:
					out.put((byte) 0x97);
					break;
				case 0x02DC:
					out.put((byte) 0x98);
					break;
				case 0x2122:
					out.put((byte) 0x99);
					break;
				case 0x0161:
					out.put((byte) 0x9A);
					break;
				case 0x203A:
					out.put((byte) 0x9B);
					break;
				case 0x0153:
					out.put((byte) 0x9C);
					break;
				case 0x009D: // This is supposed to be an unmappable character.
					out.put((byte) 0x9D);
					break;
				case 0x017E:
					out.put((byte) 0x9E);
					break;
				case 0x0178:
					out.put((byte) 0x9F);
					break;
				default:
					throw new OsmosisRuntimeException("Char 0x" + Integer.toHexString(nextValue) + " is not recognised.");
				}
			}
		}
	}
}
