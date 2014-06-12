/**
 * class TCB
 * @brief contains parent- and thread- ID for a thread, as well as maintains a 
 * history of that thread's file system operations in an array of 
 * FileTableEntry objects'
 */

public class TCB {
    private final int MAX_ENTRIES = 32;
    private final int RESERVED_ENTRIES = 3;
    private Thread thread = null;
    private int tid = 0;
    private int pid = 0;
    private boolean terminated = false;
    private int sleepTime = 0;
    public FileTableEntry[] ftEnt = null; // added for the file system
    private int fileTableSize = RESERVED_ENTRIES;

    public TCB( Thread newThread, int myTid, int parentTid ) {
		thread = newThread;
		tid = myTid;
		pid = parentTid;
		terminated = false;

		ftEnt = new FileTableEntry[MAX_ENTRIES];    // added for the file system

		System.err.println( "threadOS: a new thread (thread=" + thread + 
				    " tid=" + tid + 
				    " pid=" + pid + ")");
    }

    public synchronized Thread getThread( ) {
		return thread;
    }

    public synchronized int getTid( ) {
			return tid;
    }

    public synchronized int getPid( ) {
		return pid;
    }

    public synchronized boolean setTerminated( ) {
		terminated = true;
		return terminated;
    }

    public synchronized boolean getTerminated( ) {
		return terminated;
    }

    // added for the file system
    public synchronized int getFd( FileTableEntry entry ) {
		if ( entry == null )
		    return -1;
		for ( int i = RESERVED_ENTRIES; i < MAX_ENTRIES; i++ ) {
		    if ( ftEnt[i] == null ) {
			ftEnt[i] = entry;
			return i;
		    }
		}
		return -1;
    }

    // added for the file system
    // removes from the FIleTable and returns an FDEntry at the param index
    public synchronized FileTableEntry returnFd( int fd ) {
		if ( fd >= RESERVED_ENTRIES && fd < MAX_ENTRIES ) {
		    FileTableEntry oldEnt = ftEnt[fd];
		    ftEnt[fd] = null;

            // if this creates a hole, close it and adjust size with collapse()
            if( ( fd < (MAX_ENTRIES - 1) ) && ( ftEnt[fd + 1] != null ) )
                collapse(fd);

            return oldEnt;
        }
		//else
		    return null;
    }

    // added for the file systme
    public synchronized FileTableEntry getFtEnt( int fd ) {
		if ( fd >= RESERVED_ENTRIES && fd < MAX_ENTRIES )
		    return ftEnt[fd];
		else
		    return null;
    }

    /**
    * adds new entries to the back side of ftEnt[], exhibiting FIFOa new entry
    * TODO: make sure any 'back' commands take from the front end
    * @return the value of the index in ftEnt into which 'entry' was placed,
    * or -1 on a failure.  makes no distinction as to whether or not FIFO
    * recycling was performed
    * when the table is full, this method discards the oldest half of the
    * history, which facilitates simple writes rather than O(n) traversals
    * and writes with each call once full
    */
    public synchronized int addFTEnt(FileTableEntry entry) {

        // if the table is not full, there's no need to rewrite
        // the table
        if (!this.isTableFull()) {
            ftEnt[fileTableSize++] = entry;
            return fileTableSize;
        }

        // else we've gotta move table entries to make room for the noob
        // cut the back half of the history
        else {
            // cut the history in half
            fileTableSize /= 2;
            // now set the oldest half = newest half, replacing the old
            for (int i = RESERVED_ENTRIES; i < fileTableSize; i++)
                ftEnt[i] = ftEnt[i + fileTableSize];

            ftEnt[fileTableSize++] = entry;
            return fileTableSize;
        }
     } // end method addFTEnt

    private boolean isTableFull(){
        // yes, I know size should never go over the max, but slight
        // redundancy FTW
        return (fileTableSize >= MAX_ENTRIES);
    }

    // this method closes up a hole caused by returnFd and adjusts the size of
    // the history accordingly
    private void collapse(int index){
        for (int i = index; i < fileTableSize; i++)
            ftEnt[i] = ftEnt[i + 1];

        fileTableSize--;
        //return true;
    }
}
