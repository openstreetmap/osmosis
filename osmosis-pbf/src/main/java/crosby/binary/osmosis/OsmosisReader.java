// This software is released into the Public Domain.  See copying.txt for details.
package crosby.binary.osmosis;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collections;

import crosby.binary.file.BlockInputStream;
import org.openstreetmap.osmosis.core.OsmosisRuntimeException;
import org.openstreetmap.osmosis.core.task.v0_6.RunnableSource;
import org.openstreetmap.osmosis.core.task.v0_6.Sink;

/** Glue code that implements a task that connects an InputStream a containing binary-format data to a Sink.
 * @author crosby
 *
 */
public class OsmosisReader implements RunnableSource {
	
    private File pbfFile;
    private OsmosisBinaryParser parser;
    private Sink sink;
	
    /**
     * Make a reader based on a target input stream. 
     * @param pbfFile The PBF file to read from.
     */
    public OsmosisReader(File pbfFile) {
        this.pbfFile = pbfFile;

        parser = new OsmosisBinaryParser();
    }

    @Override
    public void setSink(Sink sink) {
    	this.sink = sink;
        parser.setSink(sink);
    }

    @Override
    public void run() {
        try {
        	sink.initialize(Collections.<String, Object>emptyMap());

            try (BlockInputStream blockInputStream = new BlockInputStream(new FileInputStream(pbfFile), parser)) {
                blockInputStream.process();
            }
            
        } catch (IOException e) {
            throw new OsmosisRuntimeException("Unable to process PBF stream", e);
        } finally {
        	sink.close();
        }
    }
}
