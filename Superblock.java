
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

	    if ( totalBlocks == diskSize && inodeBlocks > 0 && freeList >= 2))
			// disk contents are valid
			return;
		else {
			// need to format disk
			totalBlocks = diskSize;
			format( defaultInodeBlocks );
		}
	}

	public void format( int inodeBlockSize ) {
	}

	public void sync() {
		// Write back totalBlocks, inodeBlocks, and freeList to disk.
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
