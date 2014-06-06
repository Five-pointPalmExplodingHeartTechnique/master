
public class Inode {

    private final static int iNodeSize = 32;       // fix to 32 bytes
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

        for ( int i = 0; i < directSize; i++ ) {
            direct[i] = -1;
        }

        indirect = -1;
    }
// -----------------------------------------------------------------------------
    Inode( short iNumber ) {                       // retrieving inode from disk
        
        int blockNumber = 1 + iNumber / 16
        byte[] data = new byte[Disk.blockSize];
        SysLib.rawread( blockNumber, data );
        int offset = ( iNumber % 16 ) * 32;

        length = SysLib.bytes2int( data, offset );
        offset += 4;

        count = SysLib.bytes2short( data, offset );
        offset += 2;

        flag = SysLib.bytes2short( data, offset );
        offset += 2;

    }

// -----------------------------------------------------------------------------
    void toDisk( short iNumber ) {                // save to disk as the i-th inode
        int offset = 0;
        byte[] iNode = new byte[iNodeSize];

        SysLib.int2bytes( length, iNode,  offset ); // int2bytes( int i, byte[] b, int offset )
        offset += 4;

        SysLib.short2bytes( count, iNode, offset ); // short2bytes( short s, byte[] b, int offset )
        offset += 2;

        SysLib.short2bytes( flag, iNode, offset );
        offset += 2;

        // for (int i = 0; i < directSize; i++ ) {
            
        // }

    }

// -----------------------------------------------------------------------------
    short getIndexBlockNumber( ) {
        return 0;
    }

// -----------------------------------------------------------------------------
    boolean setIndexBlock( short getIndexBlockNumber ) {
        return true;
    }

// -----------------------------------------------------------------------------
    short findTargetBlock( ) {
        return indirect;
    }

// -----------------------------------------------------------------------------
    short findTargetBlock( int offset ) {
        return 0;
    }


}
