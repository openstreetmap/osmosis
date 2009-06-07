// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.apidb.v0_6;

import java.io.File;
import java.io.IOException;

import org.junit.Test;
import org.openstreetmap.osmosis.core.Osmosis;
import org.openstreetmap.osmosis.core.apidb.v0_6.impl.DatabaseUtilities;

import data.util.DataFileUtilities;


/**
 * Tests the file-based database replicator.
 */
public class ApidbFileReplicatorTest {

    private final DataFileUtilities fileUtils = new DataFileUtilities();
    private final DatabaseUtilities dbUtils = new DatabaseUtilities();


    /**
     * A basic test loading an osm file into an API database and verifying that it gets replicated correctly.
     * 
     * @throws IOException if any file operations fail.
     */
    @Test
    public void testLoadAndDump() throws IOException {
        File authFile;
        File snapshotFile;
        File changesetFile;
        File outputFile;
        File workingDirectory;

        // Generate input files.
        authFile = dbUtils.getAuthorizationFile();
        snapshotFile = fileUtils.getDataFile("v0_6/db-snapshot.osm");
        changesetFile = fileUtils.getDataFile("v0_6/db-replicate-changeset.osc");
        outputFile = File.createTempFile("test", ".osm");
        workingDirectory = fileUtils.createTempDirectory();

        // Remove all existing data from the database.
        dbUtils.truncateDatabase();
        
        // Initialise replication.
        Osmosis.run(new String[] {
        		"-q",
        		"--replicate-apidb-0.6",
        		"authFile=" + authFile.getPath(),
                "allowIncorrectSchemaVersion=true",
        		"directory=" + workingDirectory.getPath()
                });

        // Load the database with a dataset.
        Osmosis.run(new String[] {
        		"-q",
        		"--read-xml-0.6",
        		snapshotFile.getPath(),
        		"--write-apidb-0.6",
                "authFile=" + authFile.getPath(),
        		"allowIncorrectSchemaVersion=true"
                });
        
        // Run replication.
        Osmosis.run(new String[] {
        		"-q",
        		"--replicate-apidb-0.6",
        		"authFile=" + authFile.getPath(),
                "allowIncorrectSchemaVersion=true",
        		"directory=" + workingDirectory.getPath()
                });

        // Apply the changeset file to the database.
        Osmosis.run(new String[] {
        		"-q",
        		"--read-xml-change-0.6",
        		changesetFile.getPath(),
        		"--write-apidb-change-0.6",
                "authFile=" + authFile.getPath(),
        		"allowIncorrectSchemaVersion=true" });
        
        // Run replication.
        Osmosis.run(new String[] {
        		"-q",
        		"--replicate-apidb-0.6",
        		"authFile=" + authFile.getPath(),
                "allowIncorrectSchemaVersion=true",
        		"directory=" + workingDirectory.getPath()
                });
        
        // Decompress the result file.
        Osmosis.run(new String[] {
        		"-q",
        		"--read-xml-change-0.6",
        		new File(workingDirectory, "2.osc.gz").getPath(),
        		"--write-xml-change-0.6",
        		outputFile.getPath()
                });

        // Validate that the replicated file matches the input file.
        fileUtils.compareFiles(changesetFile, outputFile);

        // Success so delete the temporary files.
        outputFile.delete();
        fileUtils.deleteTempDirectory(workingDirectory);
    }
}
