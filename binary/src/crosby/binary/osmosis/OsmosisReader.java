package crosby.binary.osmosis;

import java.io.IOException;
import java.io.InputStream;

import org.openstreetmap.osmosis.core.task.v0_6.RunnableSource;
import org.openstreetmap.osmosis.core.task.v0_6.Sink;

import crosby.binary.file.BlockInputStream;

/** Glue code that implements a task that connects an InputStream a containing binary-format data to a Sink. 
 * @author crosby
 *
 */
public class OsmosisReader implements RunnableSource {
    /** Make a reader based on a target input stream. 
     * @param input 
     */
    OsmosisReader(InputStream input) {
        if (input == null) {
            throw new Error("Null input");
        }
        this.input = input;
        parser = new OsmosisBinaryParser();
    }

    @Override
    public void setSink(Sink sink) {
        parser.setSink(sink);
    }

    @Override
    public void run() {
        try {
            (new BlockInputStream(input, parser)).process();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    /** Store the input stream we're using. */
    InputStream input;
    /** The binary parser object. */
    OsmosisBinaryParser parser;
}
