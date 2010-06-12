// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.filter.common;

import java.awt.geom.Area;
import java.awt.geom.Path2D;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.openstreetmap.osmosis.core.OsmosisRuntimeException;


/**
 * Reads the contents of a polygon file into an Area instance.
 * <p>
 * The file format is defined at http://www.maproom.psu.edu/dcw/. An example is
 * provided here. The first line contains the name of the file, the second line
 * contains the name of an individual polygon and if it is prefixed with ! it
 * means it is a negative polygon to be subtracted from the resultant extraction
 * polygon.
 * <p>
 * <code>
 * australia_v<br/>
 * 1<br/>
 *      0.1446763E+03    -0.3825659E+02<br/>
 *      0.1446693E+03    -0.3826255E+02<br/>
 *      0.1446627E+03    -0.3825661E+02<br/>
 *      0.1446763E+03    -0.3824465E+02<br/>
 *      0.1446813E+03    -0.3824343E+02<br/>
 *      0.1446824E+03    -0.3824484E+02<br/>
 *      0.1446826E+03    -0.3825356E+02<br/>
 *      0.1446876E+03    -0.3825210E+02<br/>
 *      0.1446919E+03    -0.3824719E+02<br/>
 *      0.1447006E+03    -0.3824723E+02<br/>
 *      0.1447042E+03    -0.3825078E+02<br/>
 *      0.1446758E+03    -0.3826229E+02<br/>
 *      0.1446693E+03    -0.3826255E+02<br/>
 * END<br/>
 * !2<br/>
 *      0.1422483E+03    -0.3839481E+02<br/>
 *      0.1422436E+03    -0.3839315E+02<br/>
 *      0.1422496E+03    -0.3839070E+02<br/>
 *      0.1422543E+03    -0.3839025E+02<br/>
 *      0.1422574E+03    -0.3839155E+02<br/>
 *      0.1422467E+03    -0.3840065E+02<br/>
 *      0.1422433E+03    -0.3840048E+02<br/>
 *      0.1422420E+03    -0.3839857E+02<br/>
 *      0.1422436E+03    -0.3839315E+02<br/>
 * END<br/>
 * END<br/>
 * </code>
 * 
 * @author Brett Henderson
 */
public class PolygonFileReader {

	/**
	 * Our logger for debug and error -output.
	 */
    private static final Logger LOG = Logger.getLogger(PolygonFileReader.class.getName());

	/**
	 * Where we read from.
	 */
	private Reader fileReader;

	/**
	 * The filename for error-messages.
	 */
	private String polygonFile;

	/**
	 * The name of the polygon as stated in the file-header.
	 */
	private String myPolygonName;

	/**
	 * Creates a new instance.
	 * 
	 * @param polygonFile
	 *            The file to read polygon units from.
	 * @param name to report in debug output
	 */
	public PolygonFileReader(final InputStream polygonFile, final String name) {
		this.polygonFile = name;
		this.fileReader = new InputStreamReader(polygonFile);
	}

	/**
	 * Creates a new instance.
	 * 
	 * @param polygonFile
	 *            The file to read polygon units from.
	 */
	public PolygonFileReader(final File polygonFile) {
		try {
			this.polygonFile = polygonFile.getName();
			this.fileReader = new FileReader(polygonFile);
		} catch (IOException e) {
			throw new OsmosisRuntimeException("Unable to read from polygon file " + polygonFile + ".", e);
		}
	}
	
	
	/**
	 * Releases any resources remaining open.
	 */
	private void cleanup() {
		if (fileReader != null) {
			try {
				fileReader.close();
			} catch (Exception e) {
				LOG.log(Level.SEVERE, "Unable to close polygon file reader.", e);
			} finally {
				fileReader = null;
			}
		}
	}
	
	
	/**
	 * Builds an Area configured with the polygon information defined in the
	 * file.
	 * 
	 * @return A fully configured area.
	 */
	public Area loadPolygon() {
		try {
			Area resultArea;
			BufferedReader bufferedReader;
			// Create a new area.
			resultArea = new Area();

            // Open the polygon file.
            bufferedReader = new BufferedReader(fileReader);

            // Read the file header.
            myPolygonName = bufferedReader.readLine();
            if (myPolygonName == null || myPolygonName.trim().length() == 0) {
                 throw new OsmosisRuntimeException("The file must begin with a header naming the polygon file.");
            }

			// We now loop until no more sections are available.
			while (true) {
				String sectionHeader;
				boolean positivePolygon;
				Area sectionArea;
				
				// Read until a non-empty line is obtained.
				do {
					// Read the section header.
					sectionHeader = bufferedReader.readLine();
					
					// It is invalid for the file to end without a global "END" record.
					if (sectionHeader == null) {
						throw new OsmosisRuntimeException("File terminated prematurely without a section END record.");
					}
					
					// Remove any whitespace.
					sectionHeader = sectionHeader.trim();
					
				} while (sectionHeader.length() == 0);
				
				// Stop reading when the global END record is reached.
				if ("END".equals(sectionHeader)) {
					break;
				}
				
				// If the section header begins with a ! then the polygon is to
				// be subtracted from the result area.
				positivePolygon = (sectionHeader.charAt(0) != '!');
				
				// Create an area for this polygon.
				sectionArea = loadSectionPolygon(bufferedReader);
				
				// Add or subtract the section area from the overall area as
				// appropriate.
				if (positivePolygon) {
					resultArea.add(sectionArea);
				} else {
					resultArea.subtract(sectionArea);
				}
			}
			
			return resultArea;
			
		} catch (IOException e) {
			throw new OsmosisRuntimeException("Unable to read from polygon file " + polygonFile + ".", e);
		} finally {
			cleanup();
		}
	}
	
	
	/**
	 * Loads an individual polygon from the polygon file.
	 * 
	 * @param bufferedReader
	 *            The reader connected to the polygon file placed at the first
	 *            record of a polygon section.
	 * @return An area representing the section polygon.
	 */
	private Area loadSectionPolygon(BufferedReader bufferedReader) throws IOException {
		Path2D.Double polygonPath;
		double[] beginPoint = null;
		
		// Create a new path to represent this polygon.
		polygonPath = new Path2D.Double();
		
		while (true) {
			String sectionLine;
			double[] coordinates;
			
			// Read until a non-empty line is obtained.
			do {
				sectionLine = bufferedReader.readLine();
				
				// It is invalid for the file to end without a section "END" record.
				if (sectionLine == null) {
					throw new OsmosisRuntimeException("File terminated prematurely without a section END record.");
				}
				
				// Remove any whitespace.
				sectionLine = sectionLine.trim();
				
			} while (sectionLine.length() == 0);
			
			// Stop reading when the section END record is reached.
			if ("END".equals(sectionLine)) {
				break;
			}
			
			// Parse the line into its coordinates.
			coordinates = parseCoordinates(sectionLine);
			
			// Add the current point to the path.
			if (beginPoint != null) {
				polygonPath.lineTo(coordinates[0], coordinates[1]);
			} else {
				polygonPath.moveTo(coordinates[0], coordinates[1]);
				beginPoint = coordinates;
			}
		}
		
		// If we received data, draw another line from the final point back to the beginning point.
		if (beginPoint != null) {
			polygonPath.moveTo(beginPoint[0], beginPoint[1]);
		}
		
		// Convert the path into an area and return.
		return new Area(polygonPath);
	}
	
	
	/**
	 * Parses a coordinate line into its constituent double precision
	 * coordinates.
	 * 
	 * @param coordinateLine
	 *            The raw file line.
	 * @return A pair of coordinate values, first is longitude, second is
	 *         latitude.
	 */
	private double[] parseCoordinates(String coordinateLine) {
		String[] rawTokens;
		double[] results;
		int tokenCount;
		
		// Split the line into its sub strings separated by whitespace.
		rawTokens = coordinateLine.split("\\s");
		
		// Copy the non-zero tokens into a result array.
		tokenCount = 0;
		results = new double[2];
		for (int i = 0; i < rawTokens.length; i++) {
			if (rawTokens[i].length() > 0) {
				// Ensure we have no more than 2 coordinate values.
				if (tokenCount >= 2) {
					throw new OsmosisRuntimeException(
						"A polygon coordinate line must contain 2 numbers, not (" + coordinateLine + ")."
					);
				}
				
				// Parse the token into a double precision number.
				try {
					results[tokenCount++] = Double.parseDouble(rawTokens[i]);
				} catch (NumberFormatException e) {
					throw new OsmosisRuntimeException(
							"Unable to parse " + rawTokens[i] + " into a double precision number.");
				}
			}
		}
		
		// Ensure we found two tokens.
		if (tokenCount < 2) {
			throw new OsmosisRuntimeException("Could not find two coordinates on line (" + coordinateLine + ").");
		}
		
		return results;
	}

	/**
	 * This method must only be called after {@link #loadPolygon()}.
	 * @return The name of the polygon as stated in the file-header.
	 */
	public String getPolygonName() {
		return myPolygonName;
	}
}
