/*
 * BufConnectInputStream.java
 *
 * Created on 28 March 2002, 11:32
 */

package gilbert.io;
import org.apache.log4j.*;

/**
 * Reads from the Buffer of an BufConnectOutputStream.
 * This will read from the internal buffer of the given
 * BufConnectOutput stream.
 *
 * @author Daniel Hahn
 * @version CVS $Revision$
 */
public class BufConnectInputStream extends java.io.InputStream {
    /// The Output stream to connect to.
    BufConnectOutputStream out;
    /// If the reading has finished. (i.e. EOS has been reached);
    boolean readFinished = false;
    /// Logger for this class
    Logger logger = Logger.getLogger(this.getClass());
    
    /**
     * Creates a new instance of BufConnectInputStream. It is <b>not</b>
     * possible to connect one OutputStream to multiple InputStreams.
     *
     * @param out OutputStream to connect to.
     */
    public BufConnectInputStream(BufConnectOutputStream out) throws java.io.IOException {
        if (out == null) throw(new java.io.IOException("Cannot connect to null."));
        this.out = out;
    }
    
    /**
     * Reads a byte from the OutputStreams internal buffer.
     * When the buffer is empty, this will block until a
     * new buffer byte is written.
     */
    public int read() throws java.io.IOException {
        if (readFinished) throw(new java.io.IOException("Tried to read after end of stream."));
        int b = out.readByte();
        if (b < 0) readFinished = true;
        return b;
    }
    
    /**
     * Reads an array of bytes. Will only read for the number of bytes that
     * can be read without blocking. If no bytes are available, it will block
     * until at least one byte can be read.
     */
    public int read(byte[] b, int off, int len) throws java.io.IOException {
        if (b == null) throw (new NullPointerException());
        if ((off < 0) || (len < 0) || ((off + len) > b.length)) {
            throw(new ArrayIndexOutOfBoundsException());
        }
        if (logger.isDebugEnabled()) logger.debug("Attempt to read " + len + " bytes.");
        if (readFinished) throw(new java.io.IOException("Tried to read after end of stream."));
        int avail = out.available();
        int retVal = 0;
        if (out.isClosed() && (avail == 0)) {
            retVal = -1;
        } else if (len > avail) {
            if (avail == 0) {
                len = 1;
            } else {
                len = avail;
            }
        }
        int readBytes = super.read(b, off, len);
        if (logger.isDebugEnabled()) logger.debug("Read " + readBytes + " at once.");
        return readBytes;
    }
    
    public int read(byte[] b) throws java.io.IOException {
        return read(b, 0, b.length);
    }
    
    public int available() throws java.io.IOException {
        return out.available();
    }
    
    /**
     * Never skips more bytes than are in the buffer.
     */
    public long skip(long n) throws java.io.IOException {
        return out.skipBuf(n);
    }
    
}
