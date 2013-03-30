// This software is released into the Public Domain.  See copying.txt for details.
package crosby.binary.osmosis;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;

import org.openstreetmap.osmosis.core.OsmosisRuntimeException;
import org.openstreetmap.osmosis.core.task.v0_6.RunnableSource;
import org.openstreetmap.osmosis.core.task.v0_6.Sink;

import crosby.binary.file.BlockInputStream;

/** Glue code that implements a task that connects an InputStream a containing binary-format data to a Sink. 
 * @author crosby
 *
 */
public class OsmosisReader implements RunnableSource {
	
	private Sink sink;
	
    /**
     * Make a reader based on a target input stream. 
     * @param input The input stream to read from. 
     */
    public OsmosisReader(InputStream input) {
        if (input == null) {
            throw new Error("Null input");
        }
        this.input = input;
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
        	
            (new BlockInputStream(input, parser)).process();
            
        } catch (IOException e) {
            throw new OsmosisRuntimeException("Unable to process PBF stream", e);
        } finally {
        	sink.release();
        }
    }
    /** Store the input stream we're using. */
    InputStream input;
    /** The binary parser object. */
    OsmosisBinaryParser parser;
}
