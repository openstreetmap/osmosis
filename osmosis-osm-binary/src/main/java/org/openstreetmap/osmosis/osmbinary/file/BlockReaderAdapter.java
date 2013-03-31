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

/** An adaptor that receives blocks from an input stream */
public interface BlockReaderAdapter {
    /**
     * Does the reader understand this block? Does it want the data in it?
     * 
     * A reference contains the metadata about a block and can saved --- or
     * stored ---- for future random access. However, during a strea read of the
     * file, does the user want this block?
     * 
     * handleBlock will be called on all blocks that are not skipped, in file
     * order.
     * 
     * */
    boolean skipBlock(FileBlockPosition message);

    /** Called with the data in the block. */
    void handleBlock(FileBlock message);

    /** Called when the file is fully read. */
    void complete();
}
