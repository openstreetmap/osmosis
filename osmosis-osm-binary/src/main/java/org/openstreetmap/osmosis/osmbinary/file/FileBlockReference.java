/** Copyright (c) 2010 Scott A. Crosby. <scott@sacrosby.com>

   This program is free software: you can redistribute it and/or modify
   it under the terms of the GNU Lesser General Public License as 
   published by the Free Software Foundation, either version 3 of the 
   License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU General Public License for more details.

   You should have received a copy of the GNU General Public License
   along with this program.  If not, see <http://www.gnu.org/licenses/>.

*/

package org.openstreetmap.osmosis.osmbinary.file;

import java.io.IOException;
import java.io.InputStream;

import com.google.protobuf.ByteString;

/**
 * A FileBlockPosition that remembers what file this is so that it can simply be
 * dereferenced
 */
public class FileBlockReference extends FileBlockPosition {

    /**
     * Convenience cache for storing the input this reference is contained
     * within so that it can be cached
     */
    protected InputStream input;

    protected FileBlockReference(String type, ByteString indexdata) {
        super(type, indexdata);
    }

    public FileBlock read() throws IOException {
        return read(input);
    }

    static FileBlockPosition newInstance(FileBlockBase base, InputStream input,
            long offset, int length) {
        FileBlockReference out = new FileBlockReference(base.type,
                base.indexdata);
        out.datasize = length;
        out.data_offset = offset;
        out.input = input;
        return out;
    }
}
