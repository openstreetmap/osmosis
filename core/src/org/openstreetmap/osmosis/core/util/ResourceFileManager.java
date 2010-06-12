// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Logger;

import org.openstreetmap.osmosis.core.OsmosisRuntimeException;


/**
 * Contains utility methods for dealing with resource files packaged within the application.
 * 
 * @author Brett Henderson
 */
public class ResourceFileManager {
	
	private static final Logger LOG = Logger.getLogger(ResourceFileManager.class.getName());
	
	
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
		InputStream is = null;
		OutputStream os = null;
		
		try {
			byte[] buffer;
			int bytesRead;
			
			buffer = new byte[4096];
			
			is = callingClass.getResourceAsStream(sourceResource);
			os = new FileOutputStream(destinationFile);
			
			if (is == null) {
			    throw new FileNotFoundException("Could not find " + sourceResource);
			}
			
			while (true) {
				bytesRead = is.read(buffer);
				
				// Stop reading if no more data is available.
				if (bytesRead < 0) {
					break;
				}
				
				os.write(buffer, 0, bytesRead);
			}
			
			is.close();
			os.close();
			
		} catch (IOException e) {
			throw new OsmosisRuntimeException(
					"Unable to copy resource " + sourceResource + " to file " + destinationFile);
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (Exception e) {
					LOG.warning("Unable to close input stream for resource " + sourceResource);
				}
			}
			if (os != null) {
				try {
					os.close();
				} catch (Exception e) {
					LOG.warning("Unable to close output stream for file " + destinationFile);
				}
			}
		}
	}
}
