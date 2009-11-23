// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.util;

import java.io.File;

import org.openstreetmap.osmosis.core.OsmosisRuntimeException;


/**
 * Supports the creation of files in an atomic fashion. Internally it creates files using a
 * temporary filename, then renames them to the final file name.
 */
public class AtomicFileCreator {
	private File file;
	private File tmpFile;
	

	/**
	 * Creates a new instance.
	 * 
	 * @param file
	 *            The file to be created.
	 */
	public AtomicFileCreator(File file) {
		this.file = file;
		this.tmpFile = new File(file.getPath() + ".tmp");
	}
	
	
	/**
	 * Checks if either one of the main or temporary files currently exists.
	 * 
	 * @return True if a file exists, false otherwise.
	 */
	public boolean exists() {
		// We're checking both files because there is a small window where only the new file exists
		// after the main state file is deleted before the new file being renamed.
		// See http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4017593 for more details.
		return file.exists() || tmpFile.exists();
	}


	/**
	 * Renames the new temporary file to the current file deleting the current file if it exists.
	 */
	public void renameTmpFileToCurrent() {
		// Make sure we have a new file.
		if (!tmpFile.exists()) {
			throw new OsmosisRuntimeException("Can't rename non-existent file " + tmpFile + ".");
		}
		
		// Delete the existing file if it exists.
		if (file.exists()) {
			if (!file.delete()) {
				throw new OsmosisRuntimeException("Unable to delete file " + file + ".");
			}
		}
		
		// Rename the new file to the existing file.
		if (!tmpFile.renameTo(file)) {
			throw new OsmosisRuntimeException(
					"Unable to rename file " + tmpFile + " to " + file + ".");
		}
	}
	
	
	/**
	 * Returns the temporary file used during file generation. This file is written first, and then
	 * renamed to the real file name when complete.
	 * 
	 * @return The temporary file name.
	 */
	public File getTmpFile() {
		return tmpFile;
	}
	
	
	/**
	 * Returns the file represented by this class. This file is not written directly, instead a
	 * temporary file is created and then renamed to this file.
	 * 
	 * @return The file name.
	 */
	public File getFile() {
		return file;
	}
}
