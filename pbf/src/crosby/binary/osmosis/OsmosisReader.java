/**
 * Copyright 2010 Scott A. Crosby
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * @author Scott A. Crosby <scott@sacrosby.com>
 *
 */
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
    /**
     * Make a reader based on a target input stream. 
     * @param input The input stream to read from. 
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
