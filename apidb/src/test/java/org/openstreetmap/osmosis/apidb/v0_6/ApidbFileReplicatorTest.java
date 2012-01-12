// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.apidb.v0_6;

import java.io.File;
import java.io.IOException;

import org.junit.Test;
import org.openstreetmap.osmosis.apidb.v0_6.impl.DatabaseUtilities;
import org.openstreetmap.osmosis.core.Osmosis;
import org.openstreetmap.osmosis.testutil.AbstractDataTest;


/**
 * Tests the file-based database replicator.
 */
public class ApidbFileReplicatorTest extends AbstractDataTest {

    private final DatabaseUtilities dbUtils = new DatabaseUtilities(dataUtils);


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
        snapshotFile = dataUtils.createDataFile("v0_6/db-snapshot.osm");
        changesetFile = dataUtils.createDataFile("v0_6/db-replicate-changeset.osc");
        outputFile = dataUtils.newFile();
        workingDirectory = dataUtils.newFolder();

        // Remove all existing data from the database.
        dbUtils.truncateDatabase();
        
        // Initialise replication.
        Osmosis.run(new String[] {
        		"-q",
        		"--replicate-apidb-0.6",
        		"authFile=" + authFile.getPath(),
                "allowIncorrectSchemaVersion=true",
        		"--write-replication",
        		"workingDirectory=" + workingDirectory.getPath()
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
        		"--write-replication",
        		"workingDirectory=" + workingDirectory.getPath()
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
        		"--write-replication",
        		"workingDirectory=" + workingDirectory.getPath()
                });
        
        // Ensure that replication runs successfully even if no data is available.
        Osmosis.run(new String[] {
        		"-q",
        		"--replicate-apidb-0.6",
        		"authFile=" + authFile.getPath(),
                "allowIncorrectSchemaVersion=true",
        		"--write-replication",
        		"workingDirectory=" + workingDirectory.getPath()
                });
        
        // Ensure that replication can run with multiple loops.
        Osmosis.run(new String[] {
        		"-q",
        		"--replicate-apidb-0.6",
        		"authFile=" + authFile.getPath(),
                "allowIncorrectSchemaVersion=true",
        		"iterations=2",
        		"--write-replication",
        		"workingDirectory=" + workingDirectory.getPath()
                });
        
        // Decompress the result file.
        Osmosis.run(new String[] {
        		"-q",
        		"--read-xml-change-0.6",
        		new File(workingDirectory, "000/000/002.osc.gz").getPath(),
        		"--write-xml-change-0.6",
        		outputFile.getPath()
                });

        // Validate that the replicated file matches the input file.
        dataUtils.compareFiles(changesetFile, outputFile);
    }
}
