// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package org.openstreetmap.osmosis.core.xml.common;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CoderResult;

import org.openstreetmap.osmosis.core.OsmosisRuntimeException;


/**
 * A special character set decoder to work around the production OSM database
 * issue of double encoded data.
 * 
 * @author Brett Henderson
 */
public class ProductionDbDataDecoder extends CharsetDecoder {

	/**
	 * Creates a new instance.
	 * 
	 * @param cs
	 *            The owning character set.
	 * @param averageCharsPerByte
	 *            The average number of output characters per byte.
	 * @param maxCharsPerByte
	 *            The maximum number of output characters per byte.
	 */
	protected ProductionDbDataDecoder(Charset cs, float averageCharsPerByte, float maxCharsPerByte) {
		super(cs, averageCharsPerByte, maxCharsPerByte);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected CoderResult decodeLoop(ByteBuffer in, CharBuffer out) {
		while (true) {
			int nextValue;
			
			if (!in.hasRemaining()) {
				return CoderResult.UNDERFLOW;
			}
			if (!out.hasRemaining()) {
				return CoderResult.OVERFLOW;
			}
			
			// Convert to char so that we can use unsigned values from this point on.
			nextValue = in.get();
			// Ensure any high level bits are cleared.
			nextValue = nextValue & 0xFF;
			
			if (nextValue >= 0x00 && nextValue < 0x80) {
				// No translation required for this range of characters.
				out.put((char) nextValue);
			} else if (nextValue >= 0x00A0 && nextValue < 0x0100) {
				// No translation required for this range of characters.
				out.put((char) nextValue);
			} else {
				switch (nextValue) {
				case 0x80:
					out.put((char) 0x20AC);
					break;
				case 0x81: // This is supposed to be an unmappable character.
					out.put((char) 0x0081);
					break;
				case 0x82:
					out.put((char) 0x201A);
					break;
				case 0x83:
					out.put((char) 0x0192);
					break;
				case 0x84:
					out.put((char) 0x201E);
					break;
				case 0x85:
					out.put((char) 0x2026);
					break;
				case 0x86:
					out.put((char) 0x2020);
					break;
				case 0x87:
					out.put((char) 0x2021);
					break;
				case 0x88:
					out.put((char) 0x02C6);
					break;
				case 0x89:
					out.put((char) 0x2030);
					break;
				case 0x8A:
					out.put((char) 0x0160);
					break;
				case 0x8B:
					out.put((char) 0x2039);
					break;
				case 0x8C:
					out.put((char) 0x0152);
					break;
				case 0x8D: // This is supposed to be an unmappable character.
					out.put((char) 0x008D);
					break;
				case 0x8E:
					out.put((char) 0x017D);
					break;
				case 0x8F: // This is supposed to be an unmappable character.
					out.put((char) 0x008F);
					break;
				case 0x90: // This is supposed to be an unmappable character.
					out.put((char) 0x0090);
					break;
				case 0x91:
					out.put((char) 0x2018);
					break;
				case 0x92:
					out.put((char) 0x2019);
					break;
				case 0x93:
					out.put((char) 0x201C);
					break;
				case 0x94:
					out.put((char) 0x201D);
					break;
				case 0x95:
					out.put((char) 0x2022);
					break;
				case 0x96:
					out.put((char) 0x2013);
					break;
				case 0x97:
					out.put((char) 0x2014);
					break;
				case 0x98:
					out.put((char) 0x02DC);
					break;
				case 0x99:
					out.put((char) 0x2122);
					break;
				case 0x9A:
					out.put((char) 0x0161);
					break;
				case 0x9B:
					out.put((char) 0x203A);
					break;
				case 0x9C:
					out.put((char) 0x0153);
					break;
				case 0x9D: // This is supposed to be an unmappable character.
					out.put((char) 0x009D);
					break;
				case 0x9E:
					out.put((char) 0x017E);
					break;
				case 0x9F:
					out.put((char) 0x0178);
					break;
				default:
					throw new OsmosisRuntimeException(
							"Byte 0x" + Integer.toHexString(nextValue) + " is not recognised.");
				}
			}
		}
	}
}
