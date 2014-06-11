
public class FileSystem {
    private SuperBlock superblock;
    private Directory directory;
    private FileTable filetable;

    public FileSystem( int diskBlocks ) {
        // create superblock, and format disk with 64 inodes in default
        superblock = new SuperBlock( diskBlocks );

        // create directory, and register "/" in directory entry 0
        directory = new Directory( superblock.inodeBlocks );

        // file table is created, and store directory in the file table
        filetable = new FileTable( directory );

        // directory reconstruction
        FileTableEntry dirEnt = open( "/", "r" );
        int dirSize = fsize( dirEnt );
        if ( dirSize > 0 ) {
            byte[] dirData = new byte[dirSize];
            read( dirEnt, dirData );
            directory.bytes2directory( dirData );
        }
        close( dirEnt );
    }

    void sync( ) {
    }
    boolean format( int files ) {
    }
    FileTableEntry open( String filename, String mode ) {
    }
    boolean close( FileTableEntry ftEnt ) {
    }
    int fsize( FileTableEntry ftEnt ) {
    }
    int read( FileTableEntry ftEnt, byte[] buffer ) {
    }
    int write( FileTableEntry ftEnt, byte[] buffer ) {
    }
    private boolean deallocAllBlocks( FileTableEntry ftEnt ) {
        // Retrieve inode from file table entry. If inode is null, return false.
        Inode inode = ftEnt.inode;
        if(inode == null) return false;

        // Deallocate all 11 direct blocks in inode. Iterate 11 blocks until
        // reach the end of direct blocks ( hit -1 ).
        for(int i = 0; i < inode.direct.length; i++){
            // Block to be deallocate
            int blockID = inode.direct[i];
            // If hit the last block, get out of loop
            if(blockID == -1) break;
            // Return the block back to superblock
            superblock.returnBlock(blockID);
            // Set the block to -1
            inode.direct[i] = -1;
        }

        // Deallocate indirect block in inode.
        if(inode.indirect != -1){
            // Load the current indirect blocks
            byte[] b = new byte[Disk.blockSize];
            SysLib.rawread(inode.indirect, b);
            // Iterate through the indirect block until reach the end (hit -1)
            for(int i = 0; i < b.length; i += 2){
                // Get the indirect pointer
                int blockID = SysLib.bytes2short(b,i);
                // If hit the last indirect pointer, get out of loop
                if(blockID == -1) break;
                // Return the block back to superblock
                superblock.returnBlock(blockID);
                // Set the block to -1
                SysLib.short2bytes((short) -1, b, i);
            }
            // Write back indirect block
            SysLib.rawwrite(inode.indirect, b);
        }
        // Reset seek pointer to beginning of file
        ftEnt.seekPtr = 0;
        return true;
    }
    // Delete a file. Release the corresponding file table entry from directory
    boolean delete( String filename ) {
        // If given file name is empty, return false.
        if(filename.isEmpty()) return false;
        // Otherwise, retrieve the iNumber of the given file name
        short iNumber = directory.namei(filename);
        // If retrieved iNumber is -1, file doesn't exist. Return false also
        if(iNumber == -1) return false;
        // File exist, free this file table entry from directory table
        return directory.ifree(iNumber);
    }

    private final int SEEK_SET = 0;
    private final int SEEK_CUR = 1;
    private final int SEEK_END = 2;

    // Seek through file with specified locations.
    int seek( FileTableEntry ftEnt, int offset, int whence ) {
        synchronized (ftEnt){
            // Size of the current file
            int fileSize = fsize(ftEnt);
            switch (whence){
                case SEEK_SET:
                    // Seek at the beginning of file. Set file seek
                    // pointer to offset bytes.
                    ftEnt.seekPtr = offset;
                    break;
                case SEEK_CUR:
                    // Seek at the current value. Set file seek pointer
                    // to current seek pointer value plus offset bytes
                    ftEnt.seekPtr += offset;
                    break;
                case SEEK_END:
                    // Seek at the end. Set file seek pointer to the size
                    // of the file plus offset.
                    ftEnt.seekPtr = fileSize + offset;
                    break;
            }
            // If seek pointer is negative, set seek pointer to 0 (beginning of file)
            if(ftEnt.seekPtr < 0){
                ftEnt.seekPtr = 0;
            }else if(ftEnt.seekPtr > fileSize){
                // If the seek pointer is bigger than the size of the file,
                // set seek pointer to the size of file
                ftEnt.seekPtr = fileSize;
            }
        }
        return ftEnt.seekPtr;
    }
}
