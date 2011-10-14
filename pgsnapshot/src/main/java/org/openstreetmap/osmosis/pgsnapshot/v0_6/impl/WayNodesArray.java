// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.pgsnapshot.v0_6.impl;

import java.sql.Array;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Arrays;
import java.util.Map;


/**
 * Provides an Array implementation suitable for setting the way nodes column.
 * 
 * @author Brett Henderson
 */
public class WayNodesArray implements Array {

	private static String arrayToString(long[] data) {
		StringBuilder result;

		if (data == null) {
			return null;
		}

		result = new StringBuilder();

		result.append('{');
		for (int i = 0; i < data.length; i++) {
			if (i > 0) {
				result.append(',');
			}
			result.append(data[i]);
		}
		result.append('}');

		return result.toString();
	}

	
	private final long[] data;
	private final String stringValue;


	/**
	 * Creates a new instance.
	 * 
	 * @param data
	 *            The array data.
	 */
	public WayNodesArray(long[] data) {
		this.data = data;
		this.stringValue = arrayToString(data);
	}


	/**
	 * {@inheritDoc}
	 */
	public String toString() {
		return stringValue;
	}


	/**
	 * {@inheritDoc}
	 */
	public Object getArray() throws SQLException {
		if (data != null) {
			return Arrays.copyOf(data, data.length);
		} else {
			return null;
		}
	}


	/**
	 * {@inheritDoc}
	 */
	public Object getArray(Map<String, Class<?>> map) throws SQLException {
		return getArray();
	}


	/**
	 * {@inheritDoc}
	 */
	public Object getArray(long index, int count) throws SQLException {
		if (data != null) {
			return Arrays.copyOfRange(data, (int) index, (int) index + count);
		} else {
			return null;
		}
	}


	/**
	 * {@inheritDoc}
	 */
	public Object getArray(long index, int count, Map<String, Class<?>> map) throws SQLException {
		return getArray(index, count);
	}


	/**
	 * {@inheritDoc}
	 */
	public int getBaseType() throws SQLException {
		return Types.BIGINT;
	}


	/**
	 * {@inheritDoc}
	 */
	public String getBaseTypeName() throws SQLException {
		return "int8";
	}


	/**
	 * {@inheritDoc}
	 */
	public ResultSet getResultSet() throws SQLException {
		throw new UnsupportedOperationException();
	}


	/**
	 * {@inheritDoc}
	 */
	public ResultSet getResultSet(Map<String, Class<?>> map) throws SQLException {
		throw new UnsupportedOperationException();
	}


	/**
	 * {@inheritDoc}
	 */
	public ResultSet getResultSet(long index, int count) throws SQLException {
		throw new UnsupportedOperationException();
	}


	/**
	 * {@inheritDoc}
	 */
	public ResultSet getResultSet(long index, int count, Map<String, Class<?>> map) throws SQLException {
		throw new UnsupportedOperationException();
	}


	/**
	 * {@inheritDoc}
	 */
	public void free() throws SQLException {
	}
}
