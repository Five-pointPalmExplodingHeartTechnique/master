
public class Superblock {

	private final int defaultInodeBlocks = 64;
	public int totalBlocks; // the number of disk blocks
	public int totalInodes; // the number of inodes
	public int freeList;    // the block number of the free list's head

	public Superblock( int diskSize ) {
		// read the superblock from disk
		byte[] superblock = new byte[Disk.blockSize];
		SysLib.rawread( 0, superblock );
		totalBlocks = SysLib.bytes2int( superblock, 0 );
		totalInodes = SysLib.bytes2int( superblock, 4 );
		freeList = SysLib.bytes2int( superblock, 8 );

	    if ( totalBlocks == diskSize && totalInodes > 0 && freeList >= 2) {
            // disk contents are valid
            return;
        }
		else {
			// need to format disk
			totalBlocks = diskSize;
			format( defaultInodeBlocks );
		}
	}

    public void format() {
        format(defaultInodeBlocks);
    }

	public void format( int inodeBlockSize ) {
        totalInodes = inodeBlockSize;
        //INode reference
        Inode blankInode;
        //Create inodes and write them to disk
        for(int i = 0; i < totalInodes; i++) {
            blankInode = new Inode();
            blankInode.flag = 0;
            blankInode.toDisk((short)i);
        }

        //inodes are 32 bytes
        //each block on disk is 512 bytes
        //block 0 is superblock
        //free list should start at (32*inodes/512)+1
        //example 64 blocks should start at block 5 as explained in FAQ
        //example#2 48 blocks should start at block 4 as explained in FAQ
        freeList = (totalInodes*32/512)+1;

        //Create array of 0 data to write to free blocks
        byte[] zeroArray = new byte[Disk.blockSize];
        //write 0 byte data to all free blocks
        for(int i = freeList; i < totalBlocks; i++) {
            SysLib.rawwrite(i,zeroArray);
        }
        sync();
	}

	public void sync() {
		// Write back totalBlocks, inodeBlocks, and freeList to disk.
        //Disk stores bytes so we need a byte array
        byte[] toDisk = new byte[Disk.blockSize];
        //Write data to the byte array
        SysLib.int2bytes(totalBlocks,toDisk,0);
        SysLib.int2bytes(totalInodes,toDisk,4);
        SysLib.int2bytes(freeList,toDisk,8);
        //Write back to the 0 block on disk
        SysLib.rawwrite(0,toDisk);
	}

	public int getFreeBlock() {
		// Dequeue the top block from the free list.
		return 0;
	}

	public boolean returnBlock( int blockNumber ) {
		// Enqueue a given block to the end of the free list.
		return true;
	}
}
