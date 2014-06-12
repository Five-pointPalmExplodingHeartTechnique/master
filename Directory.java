/**
 * Directory.java
 * Provide a directory structure that keep track of the file sizes and 
 * file names.
 *
 * @since    6/5/2014 
 */
public class Directory {

    private static int maxChars = 30; // max characters of each file name

    // Directory entries
    private int fsizes[];        // each element stores a different file size.
    private char fnames[][];    // each element stores a different file name.

    /**
     * constructor
     * initiate the directory structure
     * 
     * @param maxInumber the number of inodes
     * 
     */
    public Directory( int maxInumber ) { // directory constructor
        fsizes = new int[maxInumber];     // maxInumber = max files
        for ( int i = 0; i < maxInumber; i++ ) 
            fsizes[i] = 0;                 // all file size initialized to 0

        fnames = new char[maxInumber][maxChars];
        
        String root = "/";                // entry(inode) 0 is "/"
        fsizes[0] = root.length( );        // fsize[0] is the size of "/".
        root.getChars( 0, fsizes[0], fnames[0], 0 ); // fnames[0] includes "/"
    }

    /**
     * bytes2directory
     * set up directory according to information given
     * 
     * @param data the bytes array from disk
     * 
     */
    public void bytes2directory( byte data[] ) {
        // assumes data[] received directory information from disk
        // initializes the Directory instance with this data[]
        // return -1 if invalid data, 0 otherwise

        // check for invalid data
        if ( ( data == null ) || ( data.length == 0 ) )
            return ;

        // initialize file sizes
        for ( int i = 0; i < fsizes.length; i++ ) {
            int offset = i * 4; // 4 bytes become 1 int
            // get file size and convert to int from bytes
            fsizes[i] = SysLib.bytes2int( data, offset );
        }

        // initialize file names
        for ( int i = 0; i < fnames.length; i++ ) {
            // each file name is 30 chars with 2 bytes for each char
            // starting at the end of the file size info
            int offset = fsizes.length * 4 + i * maxChars * 2;
            // get file name
            String currentFileName = new String( data, offset, maxChars * 2 );
            // copy file name to the corresponding array index
            currentFileName.getChars( 0, fsizes[i], fnames[i], 0 );
        }
    }

    /**
     * directory2bytes
     * convert directory information to byte array
     * 
     */
    public byte[] directory2bytes( ) {
        // converts and return Directory information into a plain byte array
        // this byte array will be written back to disk
        // note: only meaningful directory information should be converted
        // into bytes.
        // size of the array is the the number of bytes for files' length & name
        int arraySize = fsizes.length * 4 + fnames.length * maxChars * 2;
        byte[] data = new byte[arraySize]; // the plain byte array

        // convert file sizes
        for ( int i = 0; i < fsizes.length; i++ ) {
            int offset = i * 4; // 1 int becomes 4 bytes
            // convert file size from int to bytes and put in the array
            SysLib.int2bytes( fsizes[i], data, offset );
        }

        // convert file names
        for ( int i = 0; i < fnames.length; i++ ) {
            // each file name is 30 chars with 2 bytes for each char
            // starting at the end of the file size info
            int offset = fsizes.length * 4 + i * maxChars * 2;
            // get file name
            String myFileName = new String( fnames[i], 0, fsizes[i] );
            byte[] temp = myFileName.getBytes( ); // convert to bytes

            // copy to data array
            for ( int j = 0; j < temp.length; j++ ) {
                data[offset + j] = temp[j];
            }
        }

        return data; // return the plain byte array
    }

    /**
     * ialloc
     * allocation a new inode number for the given filename
     * 
     * @param filename the file to allocate the inode
     * 
     */
    public short ialloc( String filename ) {
        // filename is the one of a file to be created.
        // allocates a new inode number for this filename
        // return -1 if invalid data or unable to allocate, 0 otherwise

        // check for valid file name
        if ( ( filename == null ) || ( filename.equals( "" ) ) )
            return -1;

        // search sequentially for an empty fsize
        for ( int i = 0; i < fsizes.length; i++ ) {
            if ( i == 0 ) // fsize[0] is for root
                continue;

            if ( fsizes[i] == 0 ) { // empty slot
                // set the file size, max is 30 chars
                fsizes[i] = ( filename.length( ) > maxChars )? maxChars :
                        filename.length( );
                // set the file name
                filename.getChars( 0, fsizes[i], fnames[i], 0 );
                return (short)i; // break out of loop and return
            }
        }

        return -1; // unable to allocate
    }

    /**
     * ifree
     * deallocate the inode number 
     * 
     * @param iNumber the file to delete
     * 
     */
    public boolean ifree( short iNumber ) {
        // deallocates this inumber (inode number)
        // the corresponding file will be deleted.

        // check for valid iNumber
        if ( ( iNumber < 0 ) || ( iNumber > ( fsizes.length - 1 ) ) )
            return false;

        // reset file size to 0
        fsizes[iNumber] = 0;

        return true;
    }

    /**
     * namei
     * get the inode number of a give file 
     * 
     * @param filename the filename to get the inumber
     * 
     */
    public short namei( String filename ) {
        // returns the inumber corresponding to this filename
        // return -1 if invalid data or not found

        // check for valid file name
        if ( ( filename == null ) || ( filename.equals( "" ) ) )
            return -1;

        // perform sequential search
        for ( int i = 0; i < fsizes.length; i++ ) {
            // get the corresponding filename
            String currentFileName = new String( fnames[i], 0, fsizes[i] );
            if ( filename.equals( currentFileName ) ) { // compare string
                return (short)i; // break out of loop and return
            }
        }

        return -1; // no corresponding file name found
    }
}
