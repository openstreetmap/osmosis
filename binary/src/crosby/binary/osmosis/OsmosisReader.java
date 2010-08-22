package crosby.binary.osmosis;

import java.io.IOException;
import java.io.InputStream;

import org.openstreetmap.osmosis.core.task.v0_6.RunnableSource;
import org.openstreetmap.osmosis.core.task.v0_6.Sink;

import crosby.binary.file.BlockInputStream;

public class OsmosisReader implements RunnableSource {

    OsmosisReader(InputStream input) {
        if (input == null)
            throw new Error("Null input");
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

    InputStream input;
    OsmosisBinaryParser parser;
}
