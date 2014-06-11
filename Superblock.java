import java.util.Arrays;

public class SuperBlock {

	private final int defaultInodeBlocks = 64;
	public int totalBlocks; // the number of disk blocks
	public int totalInodes; // the number of inodes
	public int freeList;    // the block number of the free list's head

	public SuperBlock(int diskSize) {
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

        //byte array to write to each block
        byte[] zeroArray;
        //each block except the last one stores a value to the next block
        for(int i = freeList; i < totalBlocks-1; i++) {
            //In order to keep a queue, we must implement pointer in data
            zeroArray = new byte[Disk.blockSize];
            SysLib.int2bytes(i+1,zeroArray,0);
            SysLib.rawwrite(i,zeroArray);
        }
        //last block in queue stores -1
        zeroArray = new byte[Disk.blockSize];
        SysLib.int2bytes(-1,zeroArray,0);
        SysLib.rawwrite(totalBlocks-1,zeroArray);

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
        //get the free list block
        int toReturn = freeList;
        if(toReturn != -1) {
            //make array to get the next block from the free block
            byte[] temp = new byte[Disk.blockSize];
            //read from freblock
            SysLib.rawread(freeList,temp);
            //set the freeList from the current free block
            freeList = SysLib.bytes2int(temp,0);
            //fill the array to make it blank and write to the block to be returned
            Arrays.fill(temp, (byte) 0);
            SysLib.rawwrite(toReturn,temp);
        }
		return toReturn;
	}

	public boolean returnBlock( int blockNumber ) {
		// Enqueue a given block to the end of the free list.
        // set the parameter block to point to -1 and write to the parameter block
        byte[] temp;
        temp = new byte[Disk.blockSize];
        SysLib.int2bytes(-1,temp,0);
        SysLib.rawwrite(blockNumber,temp);
        //If there were no free blocks, there is now a free block
        if(freeList == -1) {
            freeList = blockNumber;
            return true;
        }
        //traverse the freeblocks until we get to the end which is the block with -1
        int current = freeList; //get the current index
        int next;
        while(current != -1) {
            temp = new byte[Disk.blockSize];
            SysLib.rawread(current,temp);
            next = SysLib.bytes2int(temp,0);
            //If block has -1, it was the previous last block, this block now points to the returned block
            if(next == -1) {
                //overwrite the number the block is pointing to with the returned block number
                SysLib.int2bytes(blockNumber,temp,0);
                //write into the current block
                SysLib.rawwrite(current,temp);
                break;
            } else {
                current = next;
            }
        }
		return true;
	}
}
