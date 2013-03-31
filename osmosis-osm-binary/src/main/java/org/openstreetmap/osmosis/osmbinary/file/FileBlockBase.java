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

import com.google.protobuf.ByteString;

/**
 * Base class that contains the metadata about a fileblock.
 * 
 * Subclasses of this include additional fields, such as byte offsets that let a
 * fileblock be read in a random-access fashion, or the data itself.
 * 
 * @author crosby
 * 
 */
public class FileBlockBase {

    /** If a block header is bigger than this, fail. We use excessively large header size as an indication of corrupt files */
    static final int MAX_HEADER_SIZE = 64*1024;
    /** If a block's size is bigger than this, fail. We use excessively large block sizes as an indication of corrupt files */
    static final int MAX_BODY_SIZE = 32*1024*1024;

    protected FileBlockBase(String type, ByteString indexdata) {
        this.type = type;
        this.indexdata = indexdata;
    }

    /** Identifies the type of the data within a block */
    protected final String type;
    /**
     * Block metadata, stored in the index block and as a prefix for every
     * block.
     */
    protected final ByteString indexdata;

    public String getType() {
        return type;
    }

    public ByteString getIndexData() {
        return indexdata;
    }
}
