/**
 * FileTable.java
 * File table consists vector to store all the file table entries 
 * and directory folder.
 *
 * @since    6/5/2014 
 */
import java.util.Vector;

public class FileTable {

    private Vector table;            // the actual entity of this file table
    private Directory dir;            // the root directory

    /**
     * constructor
     * initiate the FileTable
     * 
     * @param directory the Directory structure for this file table
     * 
     */
    public FileTable(Directory directory) { // constructor
        table = new Vector();        // instantiate a file (structure) table
        dir = directory;                // receive a reference to the Director
    }                                // from the file system

    /**
     * falloc
     * allocate a new file table entry for this file name
     * 
     * @param filename the filename to allocate
     * @param mode the access mode of the file
     * 
     */
    public synchronized FileTableEntry falloc(String filename, String mode) {

        short iNumber = -1;
        Inode inode = null;
        iNumber = filename.equals("/") ? 0 : dir.namei(filename);

        // Allocate a new file (structure) table entry for this file name
        if (iNumber >= 0) {
            inode = new Inode(iNumber);
        } else {
            // If read mode, return a null
            if (mode.equals("r")) return null;
            // Allocate/retrieve and register the corresponding inode using dir
            iNumber = dir.ialloc(filename);
            inode = new Inode();
        }

        // Increment this inode's count
        inode.count++;
        // Immediately write back this inode to the disk
        inode.toDisk(iNumber);
        // Return a reference to this file (structure) table entry
        FileTableEntry reference = new FileTableEntry(inode, iNumber, mode);
        table.addElement(reference);
        return reference;
    }

    /**
     * ffree
     * free the given file table entry from the structure 
     * 
     * @param e the file table entry to free
     * 
     */
    public synchronized boolean ffree(FileTableEntry e) {
        if (e == null) return false;

        // Receive a file table entry reference
        if (table.remove(e)) {
            // Decrement number of file table entries point to this inode
            if (e.inode.count > 0) e.inode.count--;
            // Save the corresponding inode to the disk
            e.inode.toDisk(e.iNumber);
            // Free this file table entry.
            e = null;
            // Return true if this file table entry found in my table
            return true;
        }
        // Return false if this file table entry isn't found in my table
        return false;
    }

    /**
     * fempty
     * return true if the file table is empty
     * 
     */
    public synchronized boolean fempty() {
        return table.isEmpty();
    }
}
