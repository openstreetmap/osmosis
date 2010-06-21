// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.database;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Receives result objects produced by row mapper implementations. This is used in streaming
 * scenarios where a database result set produces too many results to fit into memory.
 * 
 * @param <T> The type of object to be produced.
 */
public interface RowMapperListener<T> {
	/**
	 * Processes the provided object.
	 * 
	 * @param data
	 *            The object read from the result set.
	 * @param resultSet
	 *            The result set pointing at the current row.
	 * @throws SQLException
	 *             if an error occurs reading from the result set.
	 */
	void process(T data, ResultSet resultSet) throws SQLException;
}
