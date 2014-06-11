
public class FileSystem {
    private final int SEEK_SET = 0;
    private final int SEEK_CUR = 1;
    private final int SEEK_END = 2;

    private SuperBlock superblock;
    private Directory directory;
    private FileTable filetable;

    public FileSystem(int diskBlocks) {
        // create superblock, and format disk with 64 inodes in default
        superblock = new SuperBlock(diskBlocks);

        // create directory, and register "/" in directory entry 0
        directory = new Directory(superblock.totalInodes);

        // file table is created, and store directory in the file table
        filetable = new FileTable(directory);

        // directory reconstruction
        FileTableEntry dirEnt = open("/", "r");
        int dirSize = fsize(dirEnt);
        if (dirSize > 0) {
            byte[] dirData = new byte[dirSize];
            read(dirEnt, dirData);
            directory.bytes2directory(dirData);
        }
        close(dirEnt);
    }

    //To sync the FileSystem we need tp get the directories and write to root
    void sync() {
        //Get the entry of root
        FileTableEntry rootDir = open("/", "w");
        //get state of directories
        byte[] temp = directory.directory2bytes();
        //write into root
        write(rootDir, temp);
        close(rootDir);
        //sync superblock to write to disk
        superblock.sync();
    }

    //Format the FileSystem to support files of number in parameter
    boolean format(int files) {
        //superblock formats
        superblock.format(files);
        //create a new directory
        directory = new Directory(superblock.totalInodes);
        //create new file table
        filetable = new FileTable(directory);
        return true;
    }

    //Get the file table entry for the filename
    FileTableEntry open(String filename, String mode) {
        FileTableEntry fte = filetable.falloc(filename, mode);
        if (mode.equals("w")) {
            if (!deallocAllBlocks(fte)) {
                return null;
            }
        }
        return fte;
    }

    //Close file
    boolean close(FileTableEntry ftEnt) {
        //Synchronized to avoid simutaneous on one file
        synchronized (ftEnt) {
            //Reduce number of paths to file
            ftEnt.count--;
            //if there are still ways to access return true
            if (ftEnt.count > 0) {
                return true;
            } else {
                //No more entries to file
                return filetable.ffree(ftEnt);
            }
        }
    }

    //Return size of the file which is size of inode
    int fsize(FileTableEntry ftEnt) {
        synchronized (ftEnt) {
            return ftEnt.inode.length;
        }
    }

    int read(FileTableEntry ftEnt, byte[] buffer) {
    }

    int write(FileTableEntry ftEnt, byte[] buffer) {
    }

    private boolean deallocAllBlocks(FileTableEntry ftEnt) {
    }

    boolean delete(String filename) {
    }


    int seek(FileTableEntry ftEnt, int offset, int whence) {
    }
}
