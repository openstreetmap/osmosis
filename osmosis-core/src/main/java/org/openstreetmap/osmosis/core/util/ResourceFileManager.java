// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.openstreetmap.osmosis.core.OsmosisRuntimeException;


/**
 * Contains utility methods for dealing with resource files packaged within the application.
 * 
 * @author Brett Henderson
 */
public class ResourceFileManager {
	
	/**
	 * Copies a packaged resource to a file on the file system.
	 * 
	 * @param callingClass
	 *            The calling class is used to load the resource, this allows
	 *            resources to be loaded with paths relative to the caller.
	 * @param sourceResource
	 *            The input resource.
	 * @param destinationFile
	 *            The output file.
	 */
	public void copyResourceToFile(Class<?> callingClass, String sourceResource, File destinationFile) {
		byte[] buffer = new byte[4096];
		
		try (InputStream is = callingClass.getResourceAsStream(sourceResource)) {
			if (is == null) {
			    throw new FileNotFoundException("Could not find " + sourceResource);
			}

			try (OutputStream os = new FileOutputStream(destinationFile)) {
				while (true) {
					int bytesRead = is.read(buffer);
					
					// Stop reading if no more data is available.
					if (bytesRead < 0) {
						break;
					}
					
					os.write(buffer, 0, bytesRead);
				}
			}
			
		} catch (IOException e) {
			throw new OsmosisRuntimeException(
					"Unable to copy resource " + sourceResource + " to file " + destinationFile);
		}
	}
}
