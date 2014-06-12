/**
 * class Inode
 * represents one file in the FileSystem
 * holds the longth of its file, the number of table entries that point to this node,
 * and a flag that indicates whether it's been used (0-1) or some other status (2, 3, 4..)
 */
public class Inode {

    private final static int iNodeSize  = 32;      // fix to 32 bytes
    private final static int directSize = 11;      // # direct pointers

    public int length;                             // file size in bytes
    public short count;                            // # file-table entries pointing to this
    public short flag;                             // 0 = unused, 1 = used, ...
    public short direct[] = new short[directSize]; // direct pointers
    public short indirect;                         // a indirect pointer

// -----------------------------------------------------------------------------
// Default Constructor 
    Inode( ) {
        length = 0;
        count = 0;
        flag = 1;

        for (int i = 0; i < directSize; i++) {
            direct[i] = -1;
        }

        indirect = -1;
    }

// -----------------------------------------------------------------------------
// Constructor
// Retrieving inode from disk
    Inode( short iNumber ) {
        
        int blockNumber = 1 + iNumber / 16;
        byte[] data = new byte[Disk.blockSize];
        SysLib.rawread( blockNumber, data );
        int offset = ( iNumber % 16 ) * 32;

        // Length
        length = SysLib.bytes2int( data, offset );
        offset += 4;

        // Count
        count = SysLib.bytes2short( data, offset );
        offset += 2;

        // Flag
        flag = SysLib.bytes2short( data, offset );
        offset += 2;

        // Direct
        for (int i = 0; i < directSize; i++) {
            direct[i] = SysLib.bytes2short( data, offset);
            offset += 2;
        }

        // Indirect
        indirect = SysLib.bytes2short( data, offset);
        offset += 2;

    }

// -----------------------------------------------------------------------------
// toDisk
// Saves to the disk as the i-th inode
    public void toDisk( short iNumber ) {

        byte[] iNode = new byte[iNodeSize];
        int offset = 0;

        // Length - int2bytes( int i, byte[] b, int offset )
        SysLib.int2bytes( length, iNode,  offset );
        offset += 4;

        // Count - short2bytes( short s, byte[] b, int offset )
        SysLib.short2bytes( count, iNode, offset );
        offset += 2;

        // Flag - short2bytes( short s, byte[] b, int offset )
        SysLib.short2bytes( flag, iNode, offset );
        offset += 2;

        // Direct - short2bytes( short s, byte[] b, int offset )
        for (int i = 0; i < directSize; i++ ) {
            SysLib.short2bytes( direct[i], iNode, offset );  
            offset += 2; 
        }

        // Indirect - short2bytes( short s, byte[] b, int offset )
        SysLib.short2bytes( indirect, iNode, offset );  
        offset += 2;

        int blockNumber = 1 + iNumber / 16;
        byte[] data = new byte[Disk.blockSize];
        SysLib.rawread( blockNumber, data );
        offset = ( iNumber % 16 ) * 32;

        System.arraycopy( iNode, 0, data, offset, iNodeSize );
        SysLib.rawwrite( blockNumber, data );
    }

    public short getIndexBlockNumber( ) {
        return indirect;
    }

    // superblock.getFreeBlock()
    //public boolean setIndexBlock( short getIndexBlockNumber ) {


// ===================================================
// Testing direct and indirect
// ===================================================
        // for ( int i = 0; i < directSize; i++ ) {
        //     // Checks direct if it is -1
        //     if ( direct[i] == -1 ) {
        //         return false;
        //     }
        // }

        // if (indirect != -1) {
        //     return false;
        // }

        //indirect = getIndexBlockNumber;
        //return true;
    //}

    public short findTargetBlock( int offset ) {
        int targetBlock = offset / Disk.blockSize;
        if (targetBlock  < directSize) {
            return direct[targetBlock ];
        }

        if ( indirect < 0 ) {
            return -1;
        }

        byte[] b = new byte[Disk.blockSize];
        SysLib.rawread( indirect, b );

        return SysLib.bytes2short( b, (targetBlock  - directSize) * 2 );
    }

    // used to check SuperBlock.getFreeBlock( ) for index block
    public boolean setIndexBlock( short index ) {
        // check direct pointer
        for ( int i = 0; i < directSize; i++ ) {
            if ( direct[i] == -1 ) // not used
                return false;
        }

        // check indirect pointer
        if ( indirect != -1 ) // used
            return false;

        indirect = index; // assign value to indirect pointer
        short defaultValue = -1;

        byte[] blockData = new byte[512];
        int cycles = 512/2; // 2 bytes for 1 short

        // set defaultValue into block
        for ( int i = 0; i < cycles; i++ ) {
            SysLib.short2bytes( defaultValue, blockData, i*2 );
        }

        SysLib.rawwrite( indirect, blockData ); // write to disk

        return true;
    }

    // used to check SuperBlock.getFreeBlock( )
    public boolean setTargetBlock( int seekValue, short index ) {
        int size = seekValue / 512; // potential pointer
        if ( size < directSize ) {
            if ( direct[size] == -1 ) { // pointer not used
                // check for first pointer or if not, make sure
                // previous pointer is already used
                if ( ( size == 0 ) || ( ( size > 0 ) &&
                        ( direct[size - 1] != -1 ) ) ) {
                    direct[size] = index; // set the pointer
                    return true;
                }
            }

            return false;
        }

        // check if index block not used
        if ( indirect == -1 ) {
            return false;
        }

        byte[] blockData = new byte[512];
        // read index block from disk
        SysLib.rawread( indirect, blockData );
        int indirectNumber = size - directSize;
        // check if index block
        if ( SysLib.bytes2short( blockData, indirectNumber * 2 ) > 0 ) {
            return false;
        }

        // copy the indirect number value
        SysLib.short2bytes( index, blockData, indirectNumber * 2 );

        // store this value to disk
        SysLib.rawwrite( indirect, blockData );
        return true;
    }

}
